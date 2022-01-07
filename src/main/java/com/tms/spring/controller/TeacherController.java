package com.tms.spring.controller;

import java.util.List;
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
import com.tms.spring.model.TeacherModel;

// Repositories
import com.tms.spring.repository.UserRepository;
import com.tms.spring.repository.TeacherRepository;
import com.tms.spring.repository.TeacherSubjectTypeRepository;

// Requests
import com.tms.spring.request.SignIn.CheckLoginRequest;
import com.tms.spring.request.Teachers.AddTeacherRequest;
import com.tms.spring.request.Teachers.EditTeacherRequest;
import com.tms.spring.request.Teachers.DeleteTeacherRequest;

// Responses
import com.tms.spring.response.DefaultTeacherStatus;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/teachers")
public class TeacherController {

  @Autowired
  UserRepository userRepository;

  @Autowired
  TeacherRepository teacherRepository;

  @Autowired
  TeacherSubjectTypeRepository teacherSubjectTypeRepository;

  /* ========================================================== [ ADD TEACHER ] ======================================================= */

  @PostMapping("/add")
  public ResponseEntity<DefaultTeacherStatus> addTeacher(@RequestBody AddTeacherRequest request) {
    // In request: firstName, lastName, academicTitle, email (not obligatory), [userEmail, userToken]
    // In response: if added (OK), id

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }
    
    // Chcecking if user is not a teacher
    if(user.getType() != 1) {
      throw new NotValidException("typeNotValid");
    }

    // Input data validation
    if(request.getFirstName().length() > 50 && request.getFirstName().length() < 2) { throw new NotValidException("firstNameNotValid"); }
    if(request.getLastName().length() > 50 && request.getLastName().length() < 2) { throw new NotValidException("lastNameNotValid"); }
    if(request.getAcademicTitle().length() > 50 && request.getAcademicTitle().length() < 2) { throw new NotValidException("academicTitleNotValid"); }

    // Email is not obligatory, but if exists we have to verify it
    if(!request.getEmail().equals("")) {
      if(!request.isValidEmail()) { throw new NotValidException("emailNotValid"); }
    }

    // Saving new teacher
    TeacherModel teacher = new TeacherModel(request.getFirstName(), request.getLastName(), request.getAcademicTitle(), request.getEmail(), user);
    teacherRepository.save(teacher);

    return new ResponseEntity<>(new DefaultTeacherStatus("teacherAdded", teacher.getId()), HttpStatus.CREATED);
  }

  /* =========================================================== [ EDIT TEACHER ] ======================================================= */

  @PostMapping("/edit")
  public ResponseEntity<DefaultTeacherStatus> editTeacher(@RequestBody EditTeacherRequest request) {
    // In request: id, firstName, lastName, academicTitle, email (not obligatory), [userEmail, userToken]
    // In response: if edited (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for user's teachers to edit by id 
    TeacherModel existingTeacher = teacherRepository.findOneByIdAndUser(request.getId(), user);
    if(existingTeacher == null) {
      throw new UserNotExists("teacherNotExists");
    }

    // Input data validation
    if(request.getFirstName().length() > 50 && request.getFirstName().length() < 2) { throw new NotValidException("firstNameNotValid"); }
    if(request.getLastName().length() > 50 && request.getLastName().length() < 2) { throw new NotValidException("lastNameNotValid"); }
    if(request.getAcademicTitle().length() > 50 && request.getAcademicTitle().length() < 2) { throw new NotValidException("academicTitleNotValid"); }

    // Email is not obligatory, but if exists we have to verify it
    if(!request.getEmail().equals("")) {
      if(!request.isValidEmail()) { throw new NotValidException("emailNotValid"); }
    }
    
    // Editing and saving teacher to 'teachers' table with updated data
    existingTeacher.setFirstName(request.getFirstName());
    existingTeacher.setLastName(request.getLastName());
    existingTeacher.setAcademicTitle(request.getAcademicTitle());
    existingTeacher.setEmail(request.getEmail());
    teacherRepository.save(existingTeacher);

    return new ResponseEntity<>(new DefaultTeacherStatus("teacherEdited"), HttpStatus.CREATED);
  }

  /* ========================================================== [ DELETE TEACHER ] ====================================================== */

  @PostMapping("/delete")
  public ResponseEntity<DefaultTeacherStatus> deleteTeacher(@RequestBody DeleteTeacherRequest request) {
    // In request: id, [userEmail, userToken]
    // In response: if deleted (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for user's teachers to delete by id 
    TeacherModel teacher = teacherRepository.findOneByIdAndUser(request.getId(), user);
    if(teacher == null) {
      throw new UserNotExists("teacherNotExists");
    }

    // Deleting teacher
    teacherRepository.delete(teacher);

    return new ResponseEntity<>(new DefaultTeacherStatus("teacherDeleted"), HttpStatus.ACCEPTED);
  }

  /* =========================================================== [ GET TEACHERS ] ======================================================= */

  @PostMapping("/get")
  public ResponseEntity<List<TeacherModel>> getTeachers(@RequestBody CheckLoginRequest request) {
    // In request: type, [userEmail, userToken]
    // In response: list of teachers by userId

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if accout type is valid
    if(user.getType() != request.getType()) {
      throw new UserNotExists("typeError");
    }

    return new ResponseEntity<>(user.getTeachers(), HttpStatus.OK);
  }
}