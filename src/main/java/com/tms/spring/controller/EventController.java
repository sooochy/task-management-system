package com.tms.spring.controller;

import java.util.List;
import java.time.ZoneId;
import java.util.Arrays;
import java.time.Instant;
import java.util.stream.*;
import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.beans.factory.annotation.Autowired;

// Exceptions
import com.tms.spring.exception.UserNotExists;
import com.tms.spring.exception.NotValidException;
import com.tms.spring.exception.MaxUploadSizeExceededException;

// Models
import com.tms.spring.model.MarkModel;
import com.tms.spring.model.UserModel;
import com.tms.spring.model.TypeModel;
import com.tms.spring.model.FileModel;
import com.tms.spring.model.EventModel;
import com.tms.spring.model.TeacherModel;
import com.tms.spring.model.SubjectModel;
import com.tms.spring.model.NotificationModel;
import com.tms.spring.model.TeacherSubjectTypeModel;

// Repositories
import com.tms.spring.repository.MarkRepository;
import com.tms.spring.repository.UserRepository;
import com.tms.spring.repository.TypeRepository;
import com.tms.spring.repository.FileRepository;
import com.tms.spring.repository.EventRepository;
import com.tms.spring.repository.TeacherRepository;
import com.tms.spring.repository.SubjectRepository;
import com.tms.spring.repository.NotificationRepository;
import com.tms.spring.repository.TeacherSubjectTypeRepository;

// Requests
import com.tms.spring.request.Events.AddEventRequest;
import com.tms.spring.request.Events.EditEventRequest;
import com.tms.spring.request.Events.DeleteEventRequest;
import com.tms.spring.request.SignIn.CheckLoginRequest;

// Responses
import com.tms.spring.response.DefaultEventStatus;

// Service
import com.tms.spring.file.FileStorageService;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/events")
public class EventController {

  @Autowired
  TypeRepository typeRepository;

  @Autowired
  MarkRepository markRepository;
  
  @Autowired
  UserRepository userRepository;

  @Autowired
  FileRepository fileRepository;

  @Autowired
  EventRepository eventRepository;
  
  @Autowired
  TeacherRepository teacherRepository;

  @Autowired
  SubjectRepository subjectRepository;

  @Autowired
  FileStorageService fileStorageService;

  @Autowired
  NotificationRepository notificationRepository;
  
  @Autowired
  TeacherSubjectTypeRepository teacherSubjectTypeRepository;


  /* =========================================================== [ ADD EVENT ] ======================================================= */

  @PostMapping("/add")
  public ResponseEntity<DefaultEventStatus> addEvent(@ModelAttribute AddEventRequest request) {
    // In request: name, description (may be empty), startDate (milliseconds from date x (to calculate)), endDate (like startDate), isMarked, tstId (optional), 
    //             language, files[] (optional), notifications[] (optional), [userEmail, userToken]
    // In response: if added (OK), event

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking event's files limit (non premium user or expired subscription)
    if(user.getSubExpirationDate() == null || user.getSubExpirationDate().isBefore(LocalDateTime.now())) {
      if(request.getFiles() != null && user.getUploadedFiles() + request.getFiles().length > 10) {
        throw new NotValidException("outOfFilesLimit");
      } 
    }

    // Name and optional description check
    if(request.getName().length() < 1 || request.getName().length() > 100) { throw new NotValidException("incorrectName"); }
    if(request.getDescription() == null) { request.setDescription(""); }
    if(!request.getDescription().equals("")) { if(request.getDescription().length() < 1 || request.getDescription().length() > 2048) { throw new NotValidException("incorrectDescription"); } }

    // Event's start date validation
    LocalDateTime startDate;
    Long milliseconds;
    LocalDateTime firstPossibleDate = LocalDateTime.parse("1900-01-01T00:00:00");

    if(request.getStartDate() != null) {
      milliseconds = request.getStartDate();
      startDate = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();

      if(startDate.isBefore(firstPossibleDate)) { throw new NotValidException("incorrectStartDate"); }
    } else { throw new NotValidException("incorrectStartDate"); }

    // Event's end date validation (can not be earlier than start date time)
    LocalDateTime endDate;

    if(request.getEndDate() != null) {
      milliseconds = request.getEndDate();
      endDate = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();

      if(endDate.isBefore(startDate)) { throw new NotValidException("incorrectEndDate"); }
    } else { throw new NotValidException("incorrectEndDate"); }

    // Checking if isMarked is boolean
    if(request.getIsMarked() != true && request.getIsMarked() != false) { throw new NotValidException("incorrectIsMarked"); }
    if(user.getType() == 2) { request.setIsMarked(false); }
    
    // Looking for user's TST by id
    TeacherSubjectTypeModel teacherSubjectType;

    if(request.getTstId() != null && request.getTstId() != 0) {
      teacherSubjectType = teacherSubjectTypeRepository.findOneById(request.getTstId());
      if(teacherSubjectType == null) { throw new UserNotExists("TSTnotExists"); }

      // Checking if user has request's tstId assigned
      SubjectModel subject = subjectRepository.findOneByIdAndUser(teacherSubjectType.getSubject().getId(), user);
      TypeModel type = typeRepository.findOneByIdAndUser(teacherSubjectType.getType().getId(), user);
      
      if(user.getType() == 1) {
        TeacherModel teacher = teacherRepository.findOneByIdAndUser(teacherSubjectType.getTeacher().getId(), user);
        if(Stream.of(teacher, subject, type).anyMatch(value -> value.equals(null))) { throw new NotValidException("incorrectTST"); }
      } else {
        if(Stream.of(subject, type).anyMatch(value -> value.equals(null))) { throw new NotValidException("incorrectTST"); }
      }
    } else { 
      teacherSubjectType = null; 
      request.setIsMarked(false);
    }

    // Language validation needed for email notification
    if(request.getLanguage() != null && !request.getLanguage().equals("")) {
      switch(request.getLanguage()) {
        case "pl":
          request.setLanguage("pl");
          break;
        case "en":
          request.setLanguage("en");
          break;
        default:
          request.setLanguage("en");
          break;
      }
    } else {
      request.setLanguage("en");
    }

    // Creating and saving new event
    EventModel event = new EventModel(request.getName(), request.getDescription(), startDate, endDate, request.getIsMarked(), teacherSubjectType, user);
    eventRepository.saveAndFlush(event);

    // Saving event's files to database with eventId
    try {
      if(request.getFiles() != null) { Arrays.asList(request.getFiles()).stream().map(file -> uploadFile(file, event.getId())).collect(Collectors.toList()); }
    } catch (Exception e) {
      throw new MaxUploadSizeExceededException("fileTooLarge");
    }

    // Finding all files attached to current event
    List<FileModel> eventFiles = fileRepository.findAllByEventId(event.getId());

    // Setting files to event
    event.setFiles(eventFiles);
    
    // Creating notifications for this event
    NotificationModel notification;
    List<NotificationModel> notifications = Collections.<NotificationModel>emptyList();

    if(request.getNotifications() != null) { 
      for(Integer i = 0; i < request.getNotifications().length; i++) {
          switch(request.getNotifications()[i]) {
            case 0:
              notification = new NotificationModel(startDate, request.getLanguage(), event);
              break;
            case 1:
              notification = new NotificationModel(startDate.minus(1800, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            case 2:
              notification = new NotificationModel(startDate.minus(3600, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            case 3:
              notification = new NotificationModel(startDate.minus(21600, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            case 4:
              notification = new NotificationModel(startDate.minus(43200, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            case 5:
              notification = new NotificationModel(startDate.minus(86400, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            case 6:
              notification = new NotificationModel(startDate.minus(259200, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            case 7:
              notification = new NotificationModel(startDate.minus(604800, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            default:
              throw new NotValidException("incorrectNotification");
          }
          notificationRepository.saveAndFlush(notification);
      }
      notifications = notificationRepository.findAllByEventId(event.getId());
    }
    
    // Setting event's notifications
    event.setNotifications(notifications);

    // Creating empty mark (later to update mark of event in mark controller)
    if(request.getIsMarked() == true) {
      MarkModel mark = new MarkModel(event, teacherSubjectType);
      markRepository.saveAndFlush(mark);
    }

    return new ResponseEntity<>(new DefaultEventStatus("eventAdded", event), HttpStatus.CREATED);
  }

  /* =========================================================== [ EDIT EVENT ] ====================================================== */

  @PostMapping("/edit")
  public ResponseEntity<DefaultEventStatus> editEvent(@ModelAttribute EditEventRequest request) {
    // In request: id, name, description (may be empty), startDate (milliseconds from date x (to calculate)), endDate (like startDate), isMarked, tstId (optional), language, 
    //             files[] (optional), oldFiles[], notifications[] (optional), [userEmail, userToken]
    // In response: if added (OK), event

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for event by id
    EventModel event = eventRepository.findOneByIdAndUser(request.getId(), user);
    if(event == null) { throw new UserNotExists("eventNotExists"); }

    // Name and optional description check
    if(request.getName().length() < 1 || request.getName().length() > 100) { throw new NotValidException("incorrectName"); }
    if(request.getDescription() == null) { request.setDescription(""); }
    if(!request.getDescription().equals("")) { if(request.getDescription().length() < 1 || request.getDescription().length() > 2048) { throw new NotValidException("incorrectDescription"); } }

    // Event's start date validation
    LocalDateTime startDate;
    Long milliseconds;
    LocalDateTime firstPossibleDate = LocalDateTime.parse("1900-01-01T00:00:00");

    if(request.getStartDate() != null) {
      milliseconds = request.getStartDate();
      startDate = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();

      if(startDate.isBefore(firstPossibleDate)) { throw new NotValidException("incorrectStartDate"); }
    } else { throw new NotValidException("incorrectStartDate"); }

    // Event's end date validation (can not be earlier than start date time)
    LocalDateTime endDate;

    if(request.getEndDate() != null) {
      milliseconds = request.getEndDate();
      endDate = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();

      if(endDate.isBefore(startDate)) { throw new NotValidException("incorrectEndDate"); }
    } else { throw new NotValidException("incorrectEndDate"); }

    // Checking if isMarked is boolean
    if(request.getIsMarked() != true && request.getIsMarked() != false) { throw new NotValidException("incorrectIsMarked"); }
    if(user.getType() == 2) { request.setIsMarked(false); }

    // Looking for user's TST by id
    TeacherSubjectTypeModel teacherSubjectType;

    if(request.getTstId() != null && request.getTstId() != 0) {
      teacherSubjectType = teacherSubjectTypeRepository.findOneById(request.getTstId());
      if(teacherSubjectType == null) { throw new UserNotExists("TSTnotExists"); }

      // Checking if user has request's tstId assigned
      SubjectModel subject = subjectRepository.findOneByIdAndUser(teacherSubjectType.getSubject().getId(), user);
      TypeModel type = typeRepository.findOneByIdAndUser(teacherSubjectType.getType().getId(), user);
      
      if(user.getType() == 1) {
        TeacherModel teacher = teacherRepository.findOneByIdAndUser(teacherSubjectType.getTeacher().getId(), user);
        if(Stream.of(teacher, subject, type).anyMatch(value -> value.equals(null))) { throw new NotValidException("incorrectTST"); }
      } else {
        if(Stream.of(subject, type).anyMatch(value -> value.equals(null))) { throw new NotValidException("incorrectTST"); }
      }
    } else { 
      teacherSubjectType = null; 
    }

    // Language validation needed for email notification
    if(request.getLanguage() != null && !request.getLanguage().equals("")) {
      switch(request.getLanguage()) {
        case "pl":
          request.setLanguage("pl");
          break;
        case "en":
          request.setLanguage("en");
          break;
        default:
          request.setLanguage("en");
          break;
      }
    } else {
      request.setLanguage("en");
    }

    // Editing list of files in this event: searching the current list of event's files
    ArrayList<FileModel> listOfFiles = new ArrayList<>(event.getFiles());

    if(request.getOldFiles() != null && request.getOldFiles().length != 0) {
      for(Integer i = 0; i < request.getOldFiles().length; i++) {
        for(Integer j = 0; j < listOfFiles.size(); j++) {
          if(listOfFiles.get(j).getId().equals(request.getOldFiles()[i])) {
            listOfFiles.remove(listOfFiles.get(j));
            j--;
            break;
          }
        }
      }
    }

    // Deleting old files
    fileRepository.deleteAll(listOfFiles);
    fileRepository.flush();

    // Checking event's files limit after deleting old ones (non premium user or expired subscription)
    if(user.getSubExpirationDate() == null || user.getSubExpirationDate().isBefore(LocalDateTime.now())) {
      if(request.getFiles() != null && user.getUploadedFiles() + request.getFiles().length > 10) {
        throw new NotValidException("outOfFilesLimit");
      } 
    }

    // Saving event's files to database with eventId
    try {
      if(request.getFiles() != null) { Arrays.asList(request.getFiles()).stream().map(file -> uploadFile(file, event.getId())).collect(Collectors.toList()); }
    } catch (Exception e) {
      throw new MaxUploadSizeExceededException("fileTooLarge");
    }

    // Creating empty mark (later to update mark of event in mark controller)
    MarkModel mark;
    if(request.getIsMarked() == true) {
      if(event.getIsMarked() == false) {
        mark = new MarkModel(event, teacherSubjectType);
        markRepository.saveAndFlush(mark);
      }
    } else if (request.getIsMarked() == false) {
      if(event.getIsMarked()) {
        mark = markRepository.findOneByEventIdAndUser(event.getId(), user);
        if(mark != null) {
          markRepository.delete(mark);
        } else {
          throw new UserNotExists("eventNotExists");
        }
      }
    }

    // Setting name, description, dates and optional TSS
    EventModel editedEvent = new EventModel(request.getId(), request.getName(), request.getDescription(), startDate, endDate, request.getIsMarked(), teacherSubjectType, user);

     // Assigning files to event
     List<FileModel> eventFiles = fileRepository.findAllByEventId(event.getId());
     editedEvent.setFiles(eventFiles);

    // Edit list of notifications in this event: searching the current list of event's notifications
    ArrayList<NotificationModel> listOfNotifications = new ArrayList<>(event.getNotifications());

    // Deleting old event's notifications
    notificationRepository.deleteAll(listOfNotifications);
    notificationRepository.flush();

    // Adding new notifications for this event
    NotificationModel notification;
    List<NotificationModel> notifications = Collections.<NotificationModel>emptyList();

    if(request.getNotifications() != null) { 
      for(Integer i = 0; i < request.getNotifications().length; i++) {
          switch(request.getNotifications()[i]) {
            case 0:
              notification = new NotificationModel(startDate, request.getLanguage(), event);
              break;
            case 1:
              notification = new NotificationModel(startDate.minus(1800, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            case 2:
              notification = new NotificationModel(startDate.minus(3600, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            case 3:
              notification = new NotificationModel(startDate.minus(21600, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            case 4:
              notification = new NotificationModel(startDate.minus(43200, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            case 5:
              notification = new NotificationModel(startDate.minus(86400, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            case 6:
              notification = new NotificationModel(startDate.minus(259200, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            case 7:
              notification = new NotificationModel(startDate.minus(604800, ChronoUnit.SECONDS), request.getLanguage(), event);
              break;
            default:
              throw new NotValidException("incorrectNotification");
          }
          notificationRepository.saveAndFlush(notification);
      }
      notifications = notificationRepository.findAllByEventId(event.getId());
    }
    
    // Setting event's notifications
    editedEvent.setNotifications(notifications);

    // Saving event with edited data
    eventRepository.saveAndFlush(editedEvent);

    return new ResponseEntity<>(new DefaultEventStatus("eventEdited", editedEvent), HttpStatus.ACCEPTED);
  }

  /* ========================================================== [ DELETE EVENT ] ===================================================== */

  @PostMapping("/delete")
  public ResponseEntity<DefaultEventStatus> deleteEvent(@RequestBody DeleteEventRequest request) {
    // In request: id, [userEmail, userToken]
    // In response: if deleted (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    EventModel event = eventRepository.findOneByIdAndUser(request.getId(), user);
    if(event == null) {
      throw new UserNotExists("eventNotExists");
    }

    // Deleting event
    eventRepository.delete(event);

    return new ResponseEntity<>(new DefaultEventStatus("eventDeleted"), HttpStatus.ACCEPTED);
  }

  /* =========================================================== [ GET EVENTS ] ======================================================= */

  @PostMapping("/get")
  public ResponseEntity<List<EventModel>> getEvents(@RequestBody CheckLoginRequest request) {
    // In request: type, [userEmail, userToken]
    // In response: list of events

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if accout type is valid
    if(user.getType() != request.getType()) {
      throw new UserNotExists("typeError");
    }

    return new ResponseEntity<>(user.getEvents(), HttpStatus.OK);
  }

  /* ============================================================ [ UPLOAD FILE ] ======================================================= */

  public FileModel uploadFile(MultipartFile file, Long eventId) {
    FileModel fileModel = fileStorageService.storeEventFile(file, eventId);
    
    return fileModel;
  }
}