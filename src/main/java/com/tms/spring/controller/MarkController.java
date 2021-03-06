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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

// Exceptions
import com.tms.spring.exception.UserExists;
import com.tms.spring.exception.UserNotExists;
import com.tms.spring.exception.NotValidException;

// Models
import com.tms.spring.model.MarkModel;
import com.tms.spring.model.UserModel;
import com.tms.spring.model.TypeModel;
import com.tms.spring.model.EventModel;
import com.tms.spring.model.TeacherModel;
import com.tms.spring.model.SubjectModel;
import com.tms.spring.model.HomeworkModel;
import com.tms.spring.model.TeacherSubjectTypeModel;

// Repositories
import com.tms.spring.repository.MarkRepository;
import com.tms.spring.repository.UserRepository;
import com.tms.spring.repository.TypeRepository;
import com.tms.spring.repository.EventRepository;
import com.tms.spring.repository.TeacherRepository;
import com.tms.spring.repository.SubjectRepository;
import com.tms.spring.repository.HomeworkRepository;
import com.tms.spring.repository.TeacherSubjectTypeRepository;

// Requests
import com.tms.spring.request.Marks.AddMarkRequest;
import com.tms.spring.request.Marks.EditMarkRequest;
import com.tms.spring.request.Marks.DeleteMarkRequest;
import com.tms.spring.request.SignIn.CheckLoginRequest;

// Responses
import com.tms.spring.response.DefaultMarkStatus;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/marks")
public class MarkController {

  @Autowired
  MarkRepository markRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  TypeRepository typeRepository;

  @Autowired
  EventRepository eventRepository;
  
  @Autowired
  TeacherRepository teacherRepository;

  @Autowired
  SubjectRepository subjectRepository;

  @Autowired
  HomeworkRepository homeworkRepository;
  
  @Autowired
  TeacherSubjectTypeRepository teacherSubjectTypeRepository;

  /* =========================================================== [ ADD MARK ] ======================================================= */

  @PostMapping("/add")
  public ResponseEntity<DefaultMarkStatus> addMark(@RequestBody AddMarkRequest request) {
    // In request: mark, date, description (optional), tstId, [userEmail, userToken]
    // In response: if added (OK), mark
    
    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }
    
    // Mark validation (UK grading system is done on the frontend side)
    if(request.getMark() == null || request.getMark() < 0 || request.getMark() > 6) { throw new NotValidException("incorrectMark"); }

    // The date on which the grade was received
    LocalDateTime date;
    Long milliseconds;
    LocalDateTime firstPossibleDate = LocalDateTime.parse("1900-01-01T00:00:00");

    if(request.getDate() != null) {
      milliseconds = request.getDate();
      date = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();

      if(date.isBefore(firstPossibleDate)) { throw new NotValidException("tooEarlyDate"); }
      //if(LocalDateTime.now().isBefore(date)) { throw new NotValidException("tooLateDate"); }
    } else { throw new NotValidException("incorrectDate"); }

    // Optional description check
    if(request.getDescription() == null) { request.setDescription(""); }
    if(!request.getDescription().equals("")) { if(request.getDescription().length() < 1 || request.getDescription().length() > 2048) { throw new NotValidException("incorrectDescription"); } }
    
    // Looking for user's TST by id
    TeacherSubjectTypeModel teacherSubjectType = null;
    if(request.getTstId() != null) {
        teacherSubjectType = teacherSubjectTypeRepository.findOneById(request.getTstId());
        if(teacherSubjectType == null) { throw new UserNotExists("TSTnotExists"); }

        // Checking if user has request's tstId assigned
        TeacherModel teacher = teacherRepository.findOneByIdAndUser(teacherSubjectType.getTeacher().getId(), user);
        SubjectModel subject = subjectRepository.findOneByIdAndUser(teacherSubjectType.getSubject().getId(), user);
        TypeModel type = typeRepository.findOneByIdAndUser(teacherSubjectType.getType().getId(), user);
        
        if(Stream.of(teacher, subject, type).anyMatch(value -> value == null)) { throw new NotValidException("incorrectTST"); }
    } else { throw new NotValidException("emptyTstId"); }

    // Creating and saving new mark of event/homework/tst
    MarkModel mark = new MarkModel(request.getMark(), date, request.getDescription(), teacherSubjectType);
    markRepository.saveAndFlush(mark);

    return new ResponseEntity<>(new DefaultMarkStatus("markAdded", mark), HttpStatus.CREATED);
  }

  /* =========================================================== [ EDIT MARK ] ====================================================== */

  @PostMapping("/edit")
  public ResponseEntity<DefaultMarkStatus> editMark(@RequestBody EditMarkRequest request) {
    // In request: id, mark, date, description (optional in event or homework), [userEmail, userToken]
    // In response: if added (OK), mark
    // (EDYCJA: OPIS, DATA, OCENA)

    // TODO: Checking if date is not before start date of homework/event

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for mark by id
    MarkModel mark = markRepository.findOneByIdAndUser(request.getId(), user);
    if(mark == null) { throw new UserNotExists("markNotExists"); }

    // Mark validation 
    if(request.getMark() == null || request.getMark() < 0 || request.getMark() > 6) { throw new NotValidException("incorrectMark"); }

    // The date on which the grade was received
    LocalDateTime date;
    Long milliseconds;
    LocalDateTime firstPossibleDate = LocalDateTime.parse("1900-01-01T00:00:00");

    if(request.getDate() != null) {
      milliseconds = request.getDate();
      date = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime();

      if(date.isBefore(firstPossibleDate)) { throw new NotValidException("tooEarlyDate"); }
      // if(LocalDateTime.now().isBefore(date)) { throw new NotValidException("tooLateDate"); }
    } else { throw new NotValidException("incorrectDate"); }

    // Checking what type of mark is going to be edited
    if(Stream.of(mark.getHomework(), mark.getEvent()).allMatch(value -> value != null)) { throw new NotValidException("tooManyIds"); }

    // Optional description check (obligatory when tst, optional on events and homework):
    // if its other mark
    if(Stream.of(mark.getHomework(), mark.getEvent()).allMatch(value -> value == null)) { 
      if(request.getDescription() != null && !request.getDescription().equals("")) { 
        if(request.getDescription().length() < 1 || request.getDescription().length() > 2048) { 
          throw new NotValidException("incorrectDescription"); 
        } 
      } else {
        throw new NotValidException("emptyDescription"); 
      }
    }

    // if its homework/event mark
    if((mark.getHomework() != null && mark.getEvent() == null) || (mark.getHomework() == null && mark.getEvent() != null)) {
      if(request.getDescription() != null && !request.getDescription().equals("")) { 
        if(request.getDescription().length() < 1 || request.getDescription().length() > 2048) { 
          throw new NotValidException("incorrectDescription"); 
        } 
      } else {
        request.setDescription("");
      }
    }

    // Looking for user's event by id
    EventModel event = null;
    if(mark.getEvent() != null) {
        event = eventRepository.findOneByIdAndUser(mark.getEvent().getId(), user);
        if(event == null) { throw new UserNotExists("eventNotExists"); }
        if(event.getIsMarked() == false) { throw new UserNotExists("eventNotMarked"); }
        //if(date.isBefore(event.getStartDate())) { throw new NotValidException("incorrectEventMarkDate"); }
    }
    
    // Looking for user's homework by id
    HomeworkModel homework = null;
    if(mark.getHomework() != null) {
        homework = homeworkRepository.findOneByIdAndUser(mark.getHomework().getId(), user);
        if(homework == null) { throw new UserNotExists("homeworkNotExists"); }
        if(homework.getIsMarked() == false) { throw new UserNotExists("homeworkNotMarked"); }
        //if(date.isBefore(homework.getDate())) { throw new NotValidException("incorrectEventMarkDate"); }
    }

    // Saving new edited mark of event/homework/tst
    // MarkModel editedMark = new MarkModel(request.getId(), request.getMark(), date, request.getDescription(), event, homework, teacherSubjectType);
    mark.setMark(request.getMark());
    mark.setDate(date);
    mark.setDescription(request.getDescription());
    markRepository.saveAndFlush(mark);

    return new ResponseEntity<>(new DefaultMarkStatus("markEdited", mark), HttpStatus.ACCEPTED);
  }

  /* ========================================================== [ DELETE MARK ] ===================================================== */

  @PostMapping("/delete")
  public ResponseEntity<DefaultMarkStatus> deleteMark(@RequestBody DeleteMarkRequest request) {
    // In request: id, [userEmail, userToken]
    // In response: if deleted (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    MarkModel mark = markRepository.findOneByIdAndUser(request.getId(), user);
    if(mark == null) {
      throw new UserNotExists("markNotExists");
    }

    if(mark.getHomework() != null) {
      HomeworkModel homework = homeworkRepository.findOneByIdAndUser(mark.getHomework().getId(), user);
      homework.setIsMarked(false);
    } else if (mark.getEvent() != null) {
      EventModel event = eventRepository.findOneByIdAndUser(mark.getEvent().getId(), user);
      event.setIsMarked(false);
    }

    // Deleting mark
    markRepository.delete(mark);

    return new ResponseEntity<>(new DefaultMarkStatus("markDeleted"), HttpStatus.ACCEPTED);
  }

  /* =========================================================== [ GET MARKS ] ======================================================= */

  @PostMapping("/get")
  public ResponseEntity<List<MarkModel>> getMarks(@RequestBody CheckLoginRequest request) {
    // In request: type, [userEmail, userToken]
    // In response: list of marks

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if accout type is valid
    if(user.getType() != request.getType()) {
      throw new UserNotExists("typeError");
    }

    return new ResponseEntity<>(markRepository.findAllByUser(user), HttpStatus.OK);
  }
}