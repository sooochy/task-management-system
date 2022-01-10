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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
import com.tms.spring.model.TeacherModel;
import com.tms.spring.model.SubjectModel;
import com.tms.spring.model.HomeworkModel;
import com.tms.spring.model.NotificationModel;
import com.tms.spring.model.TeacherSubjectTypeModel;

// Repositories
import com.tms.spring.repository.MarkRepository;
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
import com.tms.spring.request.Homeworks.FinishHomeworkRequest;

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
  MarkRepository markRepository;

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
    // In request: name, description (may be empty), deadline (milliseconds from date x (to calculate), may be null), estimatedTime (in minutes), 
    //             date (like deadline), isMarked, tstId, language, files[], notifications[], [userEmail, userToken]
    // In response: if added (OK), homework

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Name and optional description check
    if(request.getName().length() < 1 || request.getName().length() > 100) { throw new NotValidException("incorrectName"); }
    if(request.getDescription() == null) { request.setDescription(""); }
    if(!request.getDescription().equals("")) { if(request.getDescription().length() < 1 || request.getDescription().length() > 2048) { throw new NotValidException("incorrectDescription"); } }

    // Deadline validation
    LocalDateTime deadline;
    Long milliseconds;

    if(request.getDeadline() != null) {
      milliseconds = request.getDeadline();
      deadline = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
    } else { throw new NotValidException("incorrectDeadline"); }

    // Estimated time check
    if(request.getEstimatedTime() != 0) { if(request.getEstimatedTime() < 0 || request.getEstimatedTime() > ((500 *60) + 59)) { throw new NotValidException("incorrectEstimatedTime"); } }

    // Date on which homework was made available to the student can be before and after current date (tasks for the future)
    milliseconds = request.getDate();
    if(milliseconds == null) { throw new NotValidException("incorrectDate"); }

    LocalDateTime date = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
    LocalDateTime firstPossibleDate = LocalDateTime.parse("1900-01-01T00:00:00");

    // Checking if date of homework is valid and if deadline is not before start date of homework
    if(date.isBefore(firstPossibleDate) || deadline.isBefore(date)) { throw new NotValidException("incorrectDate"); }
    
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
    NotificationModel notification;
    List<NotificationModel> notifications = Collections.<NotificationModel>emptyList();

    if(request.getNotifications() != null) { 
      for(Integer i = 0; i < request.getNotifications().length; i++) {
          switch(request.getNotifications()[i]) {
            case 0:
              notification = new NotificationModel(deadline, request.getLanguage(), homework);
              break;
            case 1:
              notification = new NotificationModel(deadline.minus(1800, ChronoUnit.SECONDS), request.getLanguage(), homework);
              break;
            case 2:
              notification = new NotificationModel(deadline.minus(3600, ChronoUnit.SECONDS), request.getLanguage(), homework);
              break;
            case 3:
              notification = new NotificationModel(deadline.minus(21600, ChronoUnit.SECONDS), request.getLanguage(), homework);
              break;
            case 4:
              notification = new NotificationModel(deadline.minus(43200, ChronoUnit.SECONDS), request.getLanguage(), homework);
              break;
            case 5:
              notification = new NotificationModel(deadline.minus(86400, ChronoUnit.SECONDS), request.getLanguage(), homework);
              break;
            case 6:
              notification = new NotificationModel(deadline.minus(259200, ChronoUnit.SECONDS), request.getLanguage(), homework);
              break;
            case 7:
              notification = new NotificationModel(deadline.minus(604800, ChronoUnit.SECONDS), request.getLanguage(), homework);
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
    
    // Creating empty mark (later to update mark of homework in mark controller)
    if(request.getIsMarked() == true) {
      MarkModel mark = new MarkModel(homework, teacherSubjectType, user);
      markRepository.saveAndFlush(mark);
    }

    return new ResponseEntity<>(new DefaultHomeworkStatus("homeworkAdded", homework), HttpStatus.CREATED);
  }

  /* =========================================================== [ EDIT HOMEWORK ] ====================================================== */

  @PostMapping("/edit")
  public ResponseEntity<DefaultHomeworkStatus> editHomework(@ModelAttribute EditHomeworkRequest request) {
    // In request: id, name, description (moze byc null), deadline (tak jak date oraz moze byc null), estimatedTime (w minutach), 
    //             date (liczba milisekund od daty x (do obliczenia)), isMarked, tstId, oldFiles[], files[], notifications[], [userEmail, userToken]
    // In response: if added (OK), editedHomework

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
    if(request.getDescription() == null) { request.setDescription(""); }
    if(!request.getDescription().equals("")) { if(request.getDescription().length() < 1 || request.getDescription().length() > 2048) { throw new NotValidException("incorrectDescription"); } }

    // Deadline validation
    LocalDateTime deadline;
    Long milliseconds;

    if(request.getDeadline() != null) {
      milliseconds = request.getDeadline();
      deadline = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
    } else { throw new NotValidException("incorrectDeadline"); }

    // Estimated time check
    if(request.getEstimatedTime() != 0) { if(request.getEstimatedTime() < 0 || request.getEstimatedTime() > ((500 *60) + 59)) { throw new NotValidException("incorrectEstimatedTime"); } }

    // Date on which homework was made available to the student can be before and after current date (tasks for the future)
    milliseconds = request.getDate();
    if(milliseconds == null) { throw new NotValidException("incorrectDate"); }

    LocalDateTime date = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
    LocalDateTime firstPossibleDate = LocalDateTime.parse("1900-01-01T00:00:00");

    // Checking if date of homework is valid and if deadline is not before start date of homework
    if(date.isBefore(firstPossibleDate) || deadline.isBefore(date)) { throw new NotValidException("incorrectDate"); }

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

    // Editing list of files in this homework: searching the current list of homework's files
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

    // Deleting old files
    fileRepository.deleteAll(listOfFiles);
    fileRepository.flush();

    // Saving new homework's files to database
    try {
      if(request.getFiles() != null) { Arrays.asList(request.getFiles()).stream().map(file -> uploadFile(file, homework.getId())).collect(Collectors.toList()); }
    } catch (Exception e) {
      throw new MaxUploadSizeExceededException("fileTooLarge");
    }

    // Creating empty mark (later to update mark of homework in mark controller)
    MarkModel mark;
    if(request.getIsMarked() == true) {
      if(homework.getIsMarked() == false) {
        mark = new MarkModel(homework, teacherSubjectType, user);
        markRepository.saveAndFlush(mark);
      }
    } else if (request.getIsMarked() == false) {
      if(homework.getIsMarked() == true) {
        mark = markRepository.findOneByHomeworkIdAndUser(homework.getId(), user);
        if(mark != null) {
          markRepository.delete(mark);
        } else {
          throw new UserNotExists("markNotExists");
        }
      }
    }

    // Setting name, description, date and TSS
    HomeworkModel editedHomework = new HomeworkModel(homework.getId(), request.getName(), request.getDescription(), deadline, request.getEstimatedTime(), date, request.getIsMarked(), teacherSubjectType, user);

    // Assigning files to homework
    List<FileModel> homeworkFiles = fileRepository.findAllByHomeworkId(homework.getId());
    editedHomework.setFiles(homeworkFiles);

    // Edit list of notifications in this homework: searching the current list of homework's notifications
    ArrayList<NotificationModel> listOfNotifications = new ArrayList<>(homework.getNotifications());

    // Deleting old homework's notifications
    notificationRepository.deleteAll(listOfNotifications);
    notificationRepository.flush();

    // Adding new notifications to current homework
    NotificationModel notification;
    List<NotificationModel> notifications = Collections.<NotificationModel>emptyList();

    if(request.getNotifications() != null) { 
      for(Integer i = 0; i < request.getNotifications().length; i++) {
          switch(request.getNotifications()[i]) {
            case 0:
              notification = new NotificationModel(deadline, request.getLanguage(), homework);
              break;
            case 1:
              notification = new NotificationModel(deadline.minus(1800, ChronoUnit.SECONDS), request.getLanguage(), homework);
              break;
            case 2:
              notification = new NotificationModel(deadline.minus(3600, ChronoUnit.SECONDS), request.getLanguage(), homework);
              break;
            case 3:
              notification = new NotificationModel(deadline.minus(21600, ChronoUnit.SECONDS), request.getLanguage(), homework);
              break;
            case 4:
              notification = new NotificationModel(deadline.minus(43200, ChronoUnit.SECONDS), request.getLanguage(), homework);
              break;
            case 5:
              notification = new NotificationModel(deadline.minus(86400, ChronoUnit.SECONDS), request.getLanguage(), homework);
              break;
            case 6:
              notification = new NotificationModel(deadline.minus(259200, ChronoUnit.SECONDS), request.getLanguage(), homework);
              break;
            case 7:
              notification = new NotificationModel(deadline.minus(604800, ChronoUnit.SECONDS), request.getLanguage(), homework);
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
    editedHomework.setNotifications(notifications);

    // Saving homework with edited data
    homeworkRepository.saveAndFlush(editedHomework);

    return new ResponseEntity<>(new DefaultHomeworkStatus("homeworkEdited", editedHomework), HttpStatus.ACCEPTED);
  }

  /* ========================================================== [ FINISH HOMEWORK ] ===================================================== */

  @PostMapping("/finish")
  public ResponseEntity<DefaultHomeworkStatus> finishHomework(@RequestBody FinishHomeworkRequest request) {
    // In request: id, isDone, [userEmail, userToken]
    // In response: if added (OK), finishedHomework

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for homework to finish/restore in database
    HomeworkModel finishedHomework = homeworkRepository.findOneByIdAndUser(request.getId(), user);
    if(finishedHomework == null) { throw new UserNotExists("homeworkNotExists"); }

    // Checking if isDone is boolean
    if(request.getIsDone() != true && request.getIsDone() != false) { throw new NotValidException("incorrectIsDone"); }

    // Setting new value of isDone (0 -> finished | 1 -> restored to current homeworks)
    finishedHomework.setIsDone(request.getIsDone());

    // Saving homework with edited data
    homeworkRepository.saveAndFlush(finishedHomework);

    return new ResponseEntity<>(new DefaultHomeworkStatus("homeworkFinishedOrRestored", finishedHomework), HttpStatus.ACCEPTED);
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
    // In request: type, [userEmail, userToken]
    // In response: list of plan elements

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if accout type is valid
    if(user.getType() != request.getType()) {
      throw new UserNotExists("typeError");
    }

    return new ResponseEntity<>(user.getHomeworks(), HttpStatus.OK);
  }

  /* ============================================================ [ UPLOAD FILE ] ======================================================= */

  public FileModel uploadFile(MultipartFile file, Long homeworkId) {
    FileModel fileModel = fileStorageService.storeHomeworkFile(file, homeworkId);
    
    return fileModel;
  }
}