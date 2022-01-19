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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.beans.factory.annotation.Autowired;

// Exceptions
import com.tms.spring.exception.UserExists;
import com.tms.spring.exception.UserNotExists;
import com.tms.spring.exception.NotValidException;

// Models
import com.tms.spring.model.UserModel;
import com.tms.spring.model.FacultyModel;
import com.tms.spring.model.UniversityModel;

// Repositories
import com.tms.spring.repository.UserRepository;
import com.tms.spring.repository.FacultyRepository;
import com.tms.spring.repository.UniversityRepository;

// Requests
import com.tms.spring.request.SignIn.CheckLoginRequest;
import com.tms.spring.request.Universities.AddFacultyRequest;
import com.tms.spring.request.Universities.EditFacultyRequest;
import com.tms.spring.request.Universities.DeleteFacultyRequest;
import com.tms.spring.request.Universities.AddUniversityRequest;
import com.tms.spring.request.Universities.EditUniversityRequest;
import com.tms.spring.request.Universities.DeleteUniversityRequest;

// Responses
import com.tms.spring.response.DefaultFacultyStatus;
import com.tms.spring.response.DefaultUniversityStatus;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/universities")
public class UniversityController {
  
  @Autowired
  UserRepository userRepository;
  
  @Autowired
  FacultyRepository facultyRepository;

  @Autowired
  UniversityRepository universityRepository;

  /* ************************************************************************************************************************************** */
  /*                                                               [ FACULTIES ]                                                            */
  /* ************************************************************************************************************************************** */

  /* ============================================================ [ ADD FACULTY ] ========================================================= */

  @PostMapping("/faculties/add")
  public ResponseEntity<DefaultFacultyStatus> addFaculty(@RequestBody AddFacultyRequest request) {
    // In request: name, universityId, [userEmail, userToken]
    // In response: if added (OK), faculty

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Input data validation
    if(request.getName().length() > 200 && request.getName().length() < 1) { throw new NotValidException("nameNotValid"); }

    // Check if university exists
    UniversityModel university = universityRepository.findOneByIdAndUser(request.getUniversityId(), user);
    if(university == null) {
        throw new UserNotExists("universityNotExists");
    }

    // Check if faculty already exists
    Boolean ifExists = facultyRepository.existsByNameAndUniversityAndUser(request.getName(), university, user);
    if(ifExists) {
        throw new UserExists("facultyAlreadyExists");
    }

    // Saving new faculty to database
    FacultyModel faculty = new FacultyModel(request.getName(), university);
    facultyRepository.saveAndFlush(faculty);

    return new ResponseEntity<>(new DefaultFacultyStatus("facultyAdded", faculty), HttpStatus.CREATED);
  }

  /* =========================================================== [ EDIT FACULTY ] ========================================================= */

  @PostMapping("/faculties/edit")
  public ResponseEntity<DefaultFacultyStatus> editFaculty(@RequestBody EditFacultyRequest request) {
    // In request: id, name, [userEmail, userToken]
    // In response: if added (OK), faculty

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if faculty user want to edit exists
    FacultyModel faculty = facultyRepository.findOneByIdAndUser(request.getId(), user);
    if(faculty == null) {
        throw new UserNotExists("facultyNotExists");
    }

    // Input data validation
    if(request.getName().length() > 200 && request.getName().length() < 1) { throw new NotValidException("nameNotValid"); }

    // Check if university exists
    UniversityModel university = universityRepository.findOneByIdAndUser(faculty.getUniversity().getId(), user);
    if(university == null) {
        throw new UserNotExists("universityNotExists");
    }

    // Check if faculty already exists
    Boolean ifExists = facultyRepository.existsByNameAndUniversityAndUser(request.getName(), university, user);
    if(ifExists) {
        throw new UserExists("facultyAlreadyExists");
    }

    // Saving edited faculty to database
    faculty.setName(request.getName());
    facultyRepository.saveAndFlush(faculty);

    return new ResponseEntity<>(new DefaultFacultyStatus("facultyEdited", faculty), HttpStatus.ACCEPTED);
  }

  /* ========================================================== [ DELETE FACULTY ] ======================================================== */

  @PostMapping("/faculties/delete")
  public ResponseEntity<DefaultFacultyStatus> deleteFaculty(@RequestBody DeleteFacultyRequest request) {
    // In request: id, [userEmail, userToken]
    // In response: if deleted (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for user's faculty to delete by id 
    FacultyModel faculty = facultyRepository.findOneByIdAndUser(request.getId(), user);
    if(faculty == null) {
      throw new UserNotExists("facultyNotExists");
    }
    
    // Deleting faculty from database
    facultyRepository.delete(faculty);

    return new ResponseEntity<>(new DefaultFacultyStatus("facultyDeleted"), HttpStatus.ACCEPTED);
  }

  /* ========================================================== [ GET FACULTIES ] ======================================================== */

  // @PostMapping("/faculties/get")
  // public ResponseEntity<List<FacultyModel>> deleteFaculty(@RequestBody CheckLoginRequest request) {
  //   // In request: id, [userEmail, userToken]
  //   // In response: if deleted (OK)

  //   // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
  //   UserModel user = userRepository.findOneByEmail(request.getUserEmail());
  //   if(user == null || !user.checkUser(request.getUserToken())) {
  //     throw new UserNotExists("tokenNotValid");
  //   }

  //   // Checking if accout type is valid
  //   if(user.getType() != request.getType()) {
  //     throw new UserNotExists("typeError");
  //   }

  //   return new ResponseEntity<>(user.getFaculties(), HttpStatus.OK);
  // }

  /* ************************************************************************************************************************************** */
  /*                                                              [ UNIVERSITIES ]                                                          */
  /* ************************************************************************************************************************************** */

  /* =========================================================== [ ADD UNIVERSITY ] ======================================================= */

  @PostMapping("/add")
  public ResponseEntity<DefaultUniversityStatus> addUniversity(@RequestBody AddUniversityRequest request) {
    // In request: name, [userEmail, userToken]
    // In response: if added (OK), university

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // University name validation
    if(request.getName() != null && !request.getName().equals("")) {
        if(request.getName().length() < 1 || request.getName().length() > 200) { throw new NotValidException("incorrectUniversityName"); }
    } else { 
        throw new NotValidException("emptyUnviersityName"); 
    }

    // Checking if university already exists for this user
    UniversityModel university = universityRepository.findOneByNameAndUser(request.getName(), user);
    if(university != null) {
        throw new UserExists("universityAlreadyExists");
    }

    // Creating and saving new university
    university = new UniversityModel(request.getName(), user);
    universityRepository.saveAndFlush(university);

    return new ResponseEntity<>(new DefaultUniversityStatus("universityAdded", university), HttpStatus.CREATED);
  }

  /* ========================================================= [ EDIT UNIVERSITY ] ====================================================== */

  @PostMapping("/edit")
  public ResponseEntity<DefaultUniversityStatus> editUniversity(@RequestBody EditUniversityRequest request) {
    // In request: id, name, [userEmail, userToken]
    // In response: if edited (OK), university

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
        throw new UserNotExists("tokenNotValid");
    }

    // Checking if university user want to edit exists
    UniversityModel university = universityRepository.findOneByIdAndUser(request.getId(), user);
    if(university == null) {
        throw new UserNotExists("universityNotExists");
    }

    // University name validation
    if(request.getName() != null && !request.getName().equals("")) {
        if(request.getName().length() < 1 || request.getName().length() > 200) { throw new NotValidException("incorrectUniversityName"); }
    } else { 
        throw new NotValidException("emptyUnviersityName"); 
    }

    // Checking if a university with that name to edit already exists in the database
    university = universityRepository.findOneByNameAndUser(request.getName(), user);
    if(university != null) {
        throw new UserExists("universityAlreadyExists");
    }

    // Saving university with edited data
    UniversityModel editedUniversity = new UniversityModel(request.getId(), request.getName(), user);
    universityRepository.saveAndFlush(editedUniversity);

    return new ResponseEntity<>(new DefaultUniversityStatus("universityEdited", editedUniversity), HttpStatus.ACCEPTED);
  }

  /* ======================================================== [ DELETE UNIVERSITY ] ===================================================== */

  @PostMapping("/delete")
  public ResponseEntity<DefaultUniversityStatus> deleteUniversity(@RequestBody DeleteUniversityRequest request) {
    // In request: id, [userEmail, userToken]
    // In response: if deleted (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for user's university to delete by id 
    UniversityModel university = universityRepository.findOneByIdAndUser(request.getId(), user);
    if(university == null) {
      throw new UserNotExists("universityNotExists");
    }

    // Deleting university
    universityRepository.delete(university);

    return new ResponseEntity<>(new DefaultUniversityStatus("universityDeleted"), HttpStatus.ACCEPTED);
  }

  /* =========================================================== [ GET UNIVERSITIES ] ======================================================= */

  @PostMapping("/get")
  public ResponseEntity<List<UniversityModel>> getUniversities(@RequestBody CheckLoginRequest request) {
    // In request: type, [userEmail, userToken]
    // In response: list of univerisities

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if accout type is valid
    if(user.getType() != request.getType()) {
      throw new UserNotExists("typeError");
    }

    return new ResponseEntity<>(user.getUniversities(), HttpStatus.OK);
  }
}