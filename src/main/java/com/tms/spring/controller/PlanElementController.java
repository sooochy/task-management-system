package com.tms.spring.controller;

import java.util.List;
import java.util.stream.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

// Exceptions
import com.tms.spring.exception.UserNotExists;
import com.tms.spring.exception.NotValidException;

// Models
import com.tms.spring.model.UserModel;
import com.tms.spring.model.TypeModel;
import com.tms.spring.model.TeacherModel;
import com.tms.spring.model.SubjectModel;
import com.tms.spring.model.PlanElementModel;
import com.tms.spring.model.TeacherSubjectTypeModel;

// Repositories
import com.tms.spring.repository.UserRepository;
import com.tms.spring.repository.TypeRepository;
import com.tms.spring.repository.TeacherRepository;
import com.tms.spring.repository.SubjectRepository;
import com.tms.spring.repository.PlanElementRepository;
import com.tms.spring.repository.TeacherSubjectTypeRepository;

// Requests
import com.tms.spring.request.SignIn.CheckLoginRequest;
import com.tms.spring.request.PlanElements.AddPlanElementRequest;
import com.tms.spring.request.PlanElements.EditPlanElementRequest;
import com.tms.spring.request.PlanElements.DeletePlanElementRequest;

// Responses
import com.tms.spring.response.DefaultPlanElementStatus;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/plan")
public class PlanElementController {

  @Autowired
  TypeRepository typeRepository;
  
  @Autowired
  UserRepository userRepository;
  
  @Autowired
  TeacherRepository teacherRepository;

  @Autowired
  SubjectRepository subjectRepository;
  
  @Autowired
  PlanElementRepository planElementRepository;
  
  @Autowired
  TeacherSubjectTypeRepository teacherSubjectTypeRepository;


  /* =========================================================== [ ADD PLAN ELEMENT ] ======================================================= */

  @PostMapping("/add")
  public ResponseEntity<DefaultPlanElementStatus> addPlanElement(@RequestBody AddPlanElementRequest request) {
    // In request: name, day, startTime, endTime, tstId, repetition, [userEmail, userToken]
    // In response: if added (OK), id

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // OPTIONAL TODO: Checking if similar plan element already exists in database
    // (...)
    
    // Input data validation
    if(request.getName() == null && request.getTstId() == null) { throw new NotValidException("incorrectData"); }
    if(request.getName() != null && request.getTstId() != null) { throw new NotValidException("incorrectData"); }

    if(request.getName() != null) { if(request.getName().length() < 0 || request.getName().length() > 100) { throw new NotValidException("incorrectName"); } }

    if(request.getDay() < 0 || request.getDay() > 6) { throw new NotValidException("incorrectDay"); }

    if(request.getStartTime().getHour() < 0 ||  request.getStartTime().getHour() > 23 ) { throw new NotValidException("incorrectHour"); }
    if(request.getStartTime().getMinute() < 0 ||  request.getStartTime().getMinute() > 59) { throw new NotValidException("incorrectMinutes"); }
    if(request.getStartTime().getSecond() < 0 ||  request.getStartTime().getSecond() > 59) { throw new NotValidException("incorrectSeconds"); }
    if(request.getStartTime().getNano() < 0 ||  request.getStartTime().getNano() > 999999999) { throw new NotValidException("incorrectNano"); }

    if(request.getEndTime().getHour() < 0 ||  request.getEndTime().getHour() > 23) { throw new NotValidException("incorrectHour"); }
    if(request.getEndTime().getMinute() < 0 ||  request.getEndTime().getMinute() > 59) { throw new NotValidException("incorrectMinutes"); }
    if(request.getEndTime().getSecond() < 0 ||  request.getEndTime().getSecond() > 59) { throw new NotValidException("incorrectSeconds"); }
    if(request.getEndTime().getNano() < 0 ||  request.getEndTime().getNano() > 999999999) { throw new NotValidException("incorrectNano"); }

    if(request.getRepetition() != 0 && request.getRepetition() != 1 && request.getRepetition() != 2) { throw new NotValidException("incorrectRepetition"); }
    
    Integer difference = request.getStartTime().compareTo(request.getEndTime());
    if(difference >= 0) { throw new NotValidException("incorrectTime"); }

    // Checking if request's tst exists (if was not sent by null)
    TeacherSubjectTypeModel teacherSubjectType = null;
    if(request.getTstId() != null) {
      teacherSubjectType = teacherSubjectTypeRepository.findOneById(request.getTstId());
      if(teacherSubjectType == null) { throw new UserNotExists("TSTnotExists"); }
  
      // Checking if user has request's tstId assigned
      TeacherModel teacher = teacherRepository.findOneByIdAndUser(teacherSubjectType.getTeacher().getId(), user);
      SubjectModel subject = subjectRepository.findOneByIdAndUser(teacherSubjectType.getSubject().getId(), user);
      TypeModel type = typeRepository.findOneByIdAndUser(teacherSubjectType.getType().getId(), user);
  
      if(Stream.of(teacher, subject, type).anyMatch(value -> value.equals(null))) { throw new NotValidException("incorrectTST"); }
    }

    // Checking if current element has name (if not -> has TST)
    String name = null;
    if(request.getName() != null) { name = request.getName(); }

    // Creating and saving new plan element
    PlanElementModel planElement = new PlanElementModel(name, request.getDay(), request.getStartTime(), request.getEndTime(), teacherSubjectType, user, request.getRepetition());
    planElementRepository.saveAndFlush(planElement);

    return new ResponseEntity<>(new DefaultPlanElementStatus("planElementAdded", planElement, teacherSubjectType), HttpStatus.CREATED);
  }

  /* =========================================================== [ EDIT PLAN ELEMENT ] ====================================================== */

  @PostMapping("/edit")
  public ResponseEntity<DefaultPlanElementStatus> editPlanElement(@RequestBody EditPlanElementRequest request) {
    // In request: id, name, day, startTime, endTime, tstId, repetition, [userEmail, userToken]
    // In response: if edited (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for user's plan element to edit by id 
    PlanElementModel existingPlanElement = planElementRepository.findOneByIdAndUser(request.getId(), user);
    if(existingPlanElement == null) {
      throw new UserNotExists("planElementNotExists");
    }

    // OPTIONAL TODO: Checking if similar plan element already exists in database
    // (...)

    // Input data validation
    if(request.getName() == null && request.getTstId() == null) { throw new NotValidException("incorrectData"); }
    if(request.getName() != null && request.getTstId() != null) { throw new NotValidException("incorrectData"); }

    if(request.getName() != null) { if(request.getName().length() < 0 || request.getName().length() > 100) { throw new NotValidException("incorrectName"); } }

    if(request.getDay() < 0 || request.getDay() > 6) { throw new NotValidException("incorrectDay"); }

    if(request.getStartTime().getHour() < 0 ||  request.getStartTime().getHour() > 23) { throw new NotValidException("incorrectHour"); }
    if(request.getStartTime().getMinute() < 0 ||  request.getStartTime().getMinute() > 59) { throw new NotValidException("incorrectMinutes"); }
    if(request.getStartTime().getSecond() < 0 ||  request.getStartTime().getSecond() > 59) { throw new NotValidException("incorrectSeconds"); }
    if(request.getStartTime().getNano() < 0 ||  request.getStartTime().getNano() > 999999999) { throw new NotValidException("incorrectNano"); }

    if(request.getEndTime().getHour() < 0 ||  request.getEndTime().getHour() > 23) { throw new NotValidException("incorrectHour"); }
    if(request.getEndTime().getMinute() < 0 ||  request.getEndTime().getMinute() > 59) { throw new NotValidException("incorrectMinutes"); }
    if(request.getEndTime().getSecond() < 0 ||  request.getEndTime().getSecond() > 59) { throw new NotValidException("incorrectSeconds"); }
    if(request.getEndTime().getNano() < 0 ||  request.getEndTime().getNano() > 999999999) { throw new NotValidException("incorrectNano"); }

    if(request.getRepetition() != 0 && request.getRepetition() != 1 && request.getRepetition() != 2) { throw new NotValidException("incorrectRepetition"); }
    
    Integer difference = request.getStartTime().compareTo(request.getEndTime());
    if(difference >= 0) { throw new NotValidException("incorrectTime"); }

    // Checking if request's tst exists (if was not sent by null)
    TeacherSubjectTypeModel teacherSubjectType = null;
    if(request.getTstId() != null) {
      teacherSubjectType = teacherSubjectTypeRepository.findOneById(request.getTstId());
      if(teacherSubjectType == null) { throw new UserNotExists("TSTnotExists"); }
  
      // Checking if user has request's tstId assigned
      TeacherModel teacher = teacherRepository.findOneByIdAndUser(teacherSubjectType.getTeacher().getId(), user);
      SubjectModel subject = subjectRepository.findOneByIdAndUser(teacherSubjectType.getSubject().getId(), user);
      TypeModel type = typeRepository.findOneByIdAndUser(teacherSubjectType.getType().getId(), user);
  
      if(Stream.of(teacher, subject, type).anyMatch(value -> value.equals(null))) { throw new NotValidException("incorrectTST"); }
    }

    // Checking if current element has name (if not -> has TST)
    String name = null;
    if(request.getName() != null) { name = request.getName(); }

    // Creating and saving new plan element
    PlanElementModel planElement = new PlanElementModel(request.getId(), name, request.getDay(), request.getStartTime(), request.getEndTime(), teacherSubjectType, user, request.getRepetition());
    planElementRepository.save(planElement);

    return new ResponseEntity<>(new DefaultPlanElementStatus("planElementEdited", planElement, teacherSubjectType), HttpStatus.ACCEPTED);
  }

  /* ========================================================== [ DELETE PLAN ELEMENT ] ===================================================== */

  @PostMapping("/delete")
  public ResponseEntity<DefaultPlanElementStatus> deletePlanElement(@RequestBody DeletePlanElementRequest request) {
    // In request: id, [userEmail, userToken]
    // In response: if deleted (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for user's plan element to delete by id 
    PlanElementModel planElement = planElementRepository.findOneByIdAndUser(request.getId(), user);
    if(planElement == null) {
      throw new UserNotExists("planElementNotExists");
    }

    // Deleting plan element
    planElementRepository.delete(planElement);

    return new ResponseEntity<>(new DefaultPlanElementStatus("planElementDeleted"), HttpStatus.ACCEPTED);
  }

  /* =========================================================== [ GET PLAN ELEMENT ] ======================================================= */

  @PostMapping("/get")
  public ResponseEntity<List<PlanElementModel>> getPlanElements(@RequestBody CheckLoginRequest request) {
    // In request: [userEmail, userToken]
    // In response: list of plan elements

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    return new ResponseEntity<>(user.getPlanElements(), HttpStatus.OK);
  }
}