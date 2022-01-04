package com.tms.spring.controller;

import java.util.List;
import java.time.ZoneId;
import java.util.Arrays;
import java.time.Instant;
import java.util.stream.*;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.http.MediaType;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

// Exceptions
import com.tms.spring.exception.UserNotExists;
import com.tms.spring.exception.NotValidException;
import com.tms.spring.exception.MaxUploadSizeExceededException;

// Models
import com.tms.spring.model.UserModel;
import com.tms.spring.model.TypeModel;
import com.tms.spring.model.FileModel;
import com.tms.spring.model.TeacherModel;
import com.tms.spring.model.SubjectModel;
import com.tms.spring.model.HomeworkModel;
import com.tms.spring.model.NotificationModel;
import com.tms.spring.model.TeacherSubjectTypeModel;

// Repositories
import com.tms.spring.repository.UserRepository;
import com.tms.spring.repository.TypeRepository;
import com.tms.spring.repository.FileRepository;
import com.tms.spring.repository.TeacherRepository;
import com.tms.spring.repository.SubjectRepository;
import com.tms.spring.repository.HomeworkRepository;
import com.tms.spring.repository.NotificationRepository;
import com.tms.spring.repository.TeacherSubjectTypeRepository;

// Requests
import com.tms.spring.request.SignIn.CheckLoginRequest;
import com.tms.spring.request.Homeworks.AddHomeworkRequest;
import com.tms.spring.request.Homeworks.EditHomeworkRequest;
import com.tms.spring.request.Homeworks.DeleteHomeworkRequest;

// Responses
import com.tms.spring.response.DefaultHomeworkStatus;

// Service
import com.tms.spring.file.FileStorageService;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/homework")
public class HomeworkController {

  @Autowired
  TypeRepository typeRepository;
  
  @Autowired
  UserRepository userRepository;

  @Autowired
  FileRepository fileRepository;
  
  @Autowired
  TeacherRepository teacherRepository;

  @Autowired
  SubjectRepository subjectRepository;

  @Autowired
  HomeworkRepository homeworkRepository;

  @Autowired
  FileStorageService fileStorageService;

  @Autowired
  NotificationRepository notificationRepository;
  
  @Autowired
  TeacherSubjectTypeRepository teacherSubjectTypeRepository;

  /* =========================================================== [ ADD HOMEWORK ] ======================================================= */

  @PostMapping("/add")
  public ResponseEntity<DefaultHomeworkStatus> addHomework(@ModelAttribute AddHomeworkRequest request) {
    // In request: name, description (moze byc null), deadline (tak jak date oraz moze byc null), estimatedTime (w minutach), 
    //             date (liczba milisekund od daty x (do obliczenia)), isMarked, tstId, files[], notifications[], [userEmail, userToken]
    // In response: if added (OK), id

    // TODO:
    // sprawdzanie, czy termin powiadomień nie obejmuje terminów PRZED datą otrzymania zadania

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Name and optional description check
    if(request.getName().length() < 1 || request.getName().length() > 100) { throw new NotValidException("incorrectName"); }
    if(!request.getDescription().equals("")) { if(request.getDescription().length() < 1 || request.getDescription().length() > 2048) { throw new NotValidException("incorrectDescription"); } }

    // Deadline cannot be earlier than the current date
    LocalDateTime deadline = null;
    Long milliseconds;
    if(request.getDeadline() != null) {
      milliseconds = request.getDeadline();

      deadline = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
      LocalDateTime currentDateTime = LocalDateTime.now();

      if(deadline.isBefore(currentDateTime)) { throw new NotValidException("incorrectDeadline"); }
    }

    // Estimated time check
    if(request.getEstimatedTime() != 0) { if(request.getEstimatedTime() < 0 || request.getEstimatedTime() > ((500 *60) + 59)) { throw new NotValidException("incorrectEstimatedTime"); } }

    // Date on which homework was made available to the student can be before and after current date (tasks for the future)
    milliseconds = request.getDate();
    if(milliseconds == null) { throw new NotValidException("incorrectDate"); }

    LocalDateTime date = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
    LocalDateTime firstPossibleDate = LocalDateTime.parse("1900-01-01T00:00:00");

    if(date.isBefore(firstPossibleDate)) { throw new NotValidException("incorrectDate"); }
    
    // Checking if isMarked is boolean
    if(request.getIsMarked() != true && request.getIsMarked() != false) { throw new NotValidException("incorrectIsMarked"); }

    // Looking for user's TST by id
    TeacherSubjectTypeModel teacherSubjectType = teacherSubjectTypeRepository.findOneById(request.getTstId());
    if(teacherSubjectType == null) { throw new UserNotExists("TSTnotExists"); }

    // Checking if user has request's tstId assigned
    TeacherModel teacher = teacherRepository.findOneByIdAndUser(teacherSubjectType.getTeacher().getId(), user);
    SubjectModel subject = subjectRepository.findOneByIdAndUser(teacherSubjectType.getSubject().getId(), user);
    TypeModel type = typeRepository.findOneByIdAndUser(teacherSubjectType.getType().getId(), user);
    
    if(Stream.of(teacher, subject, type).anyMatch(value -> value == null)) { throw new NotValidException("incorrectTST"); }

    // Creating and saving new homework
    HomeworkModel homework = new HomeworkModel(request.getName(), request.getDescription(), deadline, request.getEstimatedTime(), date, request.getIsMarked(), teacherSubjectType, user);
    homeworkRepository.saveAndFlush(homework);

    // Saving homework's files to database with homeworkId
    try {
      if(request.getFiles() != null) { Arrays.asList(request.getFiles()).stream().map(file -> uploadFile(file, homework.getId())).collect(Collectors.toList()); }
    } catch (Exception e) {
      throw new MaxUploadSizeExceededException("fileTooLarge");
    }

    // Finding all files attached to current homework
    List<FileModel> homeworkFiles = fileRepository.findAllByHomeworkId(homework.getId());

    // Setting files to homework
    homework.setFiles(homeworkFiles);

    // Creating notifications for this homework
    NotificationModel notification = null;
    List<NotificationModel> notifications = null;

    if(request.getNotifications() != null) { 
      for(Integer i = 0; i < request.getNotifications().length; i++) {
          switch(request.getNotifications()[i]) {
            case 0:
              notification = new NotificationModel(deadline, homework);
              break;
            case 1:
              notification = new NotificationModel(deadline.minus(1800, ChronoUnit.SECONDS), homework);
              break;
            case 2:
              notification = new NotificationModel(deadline.minus(3600, ChronoUnit.SECONDS), homework);
              break;
            case 3:
              notification = new NotificationModel(deadline.minus(21600, ChronoUnit.SECONDS), homework);
              break;
            case 4:
              notification = new NotificationModel(deadline.minus(43200, ChronoUnit.SECONDS), homework);
              break;
            case 5:
              notification = new NotificationModel(deadline.minus(86400, ChronoUnit.SECONDS), homework);
              break;
            case 6:
              notification = new NotificationModel(deadline.minus(259200, ChronoUnit.SECONDS), homework);
              break;
            case 7:
              notification = new NotificationModel(deadline.minus(604800, ChronoUnit.SECONDS), homework);
              break;
            default:
              throw new NotValidException("incorrectNotification");
          }
          if(notification.getAlertDate().isBefore(date)) { throw new NotValidException("incorrectNotification"); }
          notificationRepository.saveAndFlush(notification);
      }
      notifications = notificationRepository.findAllByHomeworkId(homework.getId());
    }

    // Setting homework's notifications
    homework.setNotifications(notifications);
    
    return new ResponseEntity<>(new DefaultHomeworkStatus("homeworkAdded", homework), HttpStatus.CREATED);
  }

  /* =========================================================== [ EDIT HOMEWORK ] ====================================================== */

  @PostMapping("/edit")
  public ResponseEntity<DefaultHomeworkStatus> editHomework(@RequestBody EditHomeworkRequest request) {
    // In request: id, name, description (moze byc null), deadline (tak jak date oraz moze byc null), estimatedTime (w minutach), 
    //             date (liczba milisekund od daty x (do obliczenia)), isMarked, isDone, tstId, oldFiles[], files[], notifications[], [userEmail, userToken]
    // In response: if added (OK), id

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for homework by id
    HomeworkModel homework = homeworkRepository.findOneByIdAndUser(request.getId(), user);
    if(homework == null) { throw new UserNotExists("homeworkNotExists"); }

    // Name and optional description check
    if(request.getName().length() < 1 || request.getName().length() > 100) { throw new NotValidException("incorrectName"); }
    if(!request.getDescription().equals("")) { if(request.getDescription().length() < 1 || request.getDescription().length() > 2048) { throw new NotValidException("incorrectDescription"); } }

    // Deadline cannot be earlier than the current date
    LocalDateTime deadline = null;
    Long milliseconds;
    if(request.getDeadline() != null) {
      milliseconds = request.getDeadline();

      deadline = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
      LocalDateTime currentDateTime = LocalDateTime.now();

      if(deadline.isBefore(currentDateTime)) { throw new NotValidException("incorrectDeadline"); }
    }

    // Estimated time check
    if(request.getEstimatedTime() != 0) { if(request.getEstimatedTime() < 0 || request.getEstimatedTime() > ((500 *60) + 59)) { throw new NotValidException("incorrectEstimatedTime"); } }

    // Date on which homework was made available to the student can be before and after current date (tasks for the future)
    milliseconds = request.getDate();
    if(milliseconds == null) { throw new NotValidException("incorrectDate"); }

    LocalDateTime date = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
    LocalDateTime firstPossibleDate = LocalDateTime.parse("1900-01-01T00:00:00");

    if(date.isBefore(firstPossibleDate)) { throw new NotValidException("incorrectDate"); }

    // Booleans check
    if(Stream.of(request.getIsMarked(), request.getIsDone()).anyMatch(value -> value == null)) { throw new NotValidException("incorrectMarkedOrDone"); }

    // Looking for user's TST by id
    TeacherSubjectTypeModel teacherSubjectType = teacherSubjectTypeRepository.findOneById(request.getTstId());
    if(teacherSubjectType == null) { throw new UserNotExists("TSTnotExists"); }

    // Checking if user has request's tstId assigned
    TeacherModel teacher = teacherRepository.findOneByIdAndUser(teacherSubjectType.getTeacher().getId(), user);
    SubjectModel subject = subjectRepository.findOneByIdAndUser(teacherSubjectType.getSubject().getId(), user);
    TypeModel type = typeRepository.findOneByIdAndUser(teacherSubjectType.getType().getId(), user);
    
    if(Stream.of(teacher, subject, type).anyMatch(value -> value == null)) { throw new NotValidException("incorrectTST"); }

    // Editing list of files in this homework:
    // Searching the current list of homework's files
    ArrayList<FileModel> listOfFiles = new ArrayList<>(homework.getFiles());

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
    fileRepository.deleteAll(listOfFiles);

    // Saving new homework's files to database
    try {
      if(request.getFiles() != null) { Arrays.asList(request.getFiles()).stream().map(file -> uploadFile(file, homework.getId())).collect(Collectors.toList()); }
    } catch (Exception e) {
      throw new MaxUploadSizeExceededException("fileTooLarge");
    }

    // Setting name, description, date and TSS
    HomeworkModel editedHomework = new HomeworkModel(homework.getId(), request.getName(), request.getDescription(), deadline, request.getEstimatedTime(), date, request.getIsMarked(), request.getIsDone(), teacherSubjectType, user);

    // Assigning files to homework
    List<FileModel> homeworkFiles = fileRepository.findAllByHomeworkId(homework.getId());
    editedHomework.setFiles(homeworkFiles);

    // Edit list of notifications in this homework:
    // Searching the current list of homework's notifications
    ArrayList<NotificationModel> listOfNotifications = new ArrayList<>(homework.getNotifications());

    if(request.getOldNotifications() != null && request.getOldNotifications().length != 0) {
      for(Integer i = 0; i < request.getOldNotifications().length; i++) {
        for(Integer j = 0; j < listOfNotifications.size(); j++) {
          if(listOfNotifications.get(j).getId().equals(request.getOldNotifications()[i])) {
            listOfNotifications.remove(listOfNotifications.get(j));
            j--;
            break;
          }
        }
      }
    }
    notificationRepository.deleteAll(listOfNotifications);

    // Adding new notifications to current homework
    NotificationModel notification = null;
    List<NotificationModel> notifications = null;

    if(request.getNotifications() != null) { 
      for(Integer i = 0; i < request.getNotifications().length; i++) {
          switch(request.getNotifications()[i]) {
            case 0:
              notification = new NotificationModel(deadline, homework);
              break;
            case 1:
              notification = new NotificationModel(deadline.minus(1800, ChronoUnit.SECONDS), homework);
              break;
            case 2:
              notification = new NotificationModel(deadline.minus(3600, ChronoUnit.SECONDS), homework);
              break;
            case 3:
              notification = new NotificationModel(deadline.minus(21600, ChronoUnit.SECONDS), homework);
              break;
            case 4:
              notification = new NotificationModel(deadline.minus(43200, ChronoUnit.SECONDS), homework);
              break;
            case 5:
              notification = new NotificationModel(deadline.minus(86400, ChronoUnit.SECONDS), homework);
              break;
            case 6:
              notification = new NotificationModel(deadline.minus(259200, ChronoUnit.SECONDS), homework);
              break;
            case 7:
              notification = new NotificationModel(deadline.minus(604800, ChronoUnit.SECONDS), homework);
              break;
            default:
              throw new NotValidException("incorrectNotification");
          }
          if(notification.getAlertDate().isBefore(date)) { throw new NotValidException("incorrectNotification"); }
          notificationRepository.saveAndFlush(notification);
      }
      notifications = notificationRepository.findAllByHomeworkId(homework.getId());
    }

    // Setting homework's notifications
    homework.setNotifications(notifications);

    // Saving material with edited data
    homeworkRepository.saveAndFlush(editedHomework);

    return new ResponseEntity<>(new DefaultHomeworkStatus("homeworkEdited", editedHomework), HttpStatus.ACCEPTED);
  }

  /* ========================================================== [ DELETE HOMEWORK ] ===================================================== */

  @PostMapping("/delete")
  public ResponseEntity<DefaultHomeworkStatus> deleteHomework(@RequestBody DeleteHomeworkRequest request) {
    // In request: id, [userEmail, userToken]
    // In response: if deleted (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    HomeworkModel homework = homeworkRepository.findOneByIdAndUser(request.getId(), user);
    if(homework == null) {
      throw new UserNotExists("homeworkNotExists");
    }

    // Deleting homework
    homeworkRepository.delete(homework);

    return new ResponseEntity<>(new DefaultHomeworkStatus("homeworkDeleted"), HttpStatus.ACCEPTED);
  }

  /* =========================================================== [ GET HOMEWORK ] ======================================================= */

  @PostMapping("/get")
  public ResponseEntity<List<HomeworkModel>> getHomework(@RequestBody CheckLoginRequest request) {
    // In request: [userEmail, userToken]
    // In response: list of plan elements

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    return new ResponseEntity<>(user.getHomeworks(), HttpStatus.OK);
  }

  /* ============================================================== [ ADD FILE ] ========================================================= */

  public FileModel uploadFile(MultipartFile file, Long homeworkId) {
    FileModel fileModel = fileStorageService.storeHomeworkFile(file, homeworkId);
    
    return fileModel;
  }
}