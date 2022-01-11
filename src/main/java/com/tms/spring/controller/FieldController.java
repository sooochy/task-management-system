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
import com.tms.spring.model.FieldModel;
import com.tms.spring.model.FacultyModel;
import com.tms.spring.model.UniversityModel;

// Repositories
import com.tms.spring.repository.UserRepository;
import com.tms.spring.repository.FieldRepository;
import com.tms.spring.repository.FacultyRepository;

// Requests
import com.tms.spring.request.Fields.AddFieldRequest;
import com.tms.spring.request.Fields.EditFieldRequest;
import com.tms.spring.request.SignIn.CheckLoginRequest;
import com.tms.spring.request.Fields.DeleteFieldRequest;

// Responses
import com.tms.spring.response.DefaultFieldStatus;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/fields")
public class FieldController {
  
  @Autowired
  UserRepository userRepository;

  @Autowired
  FieldRepository fieldRepository;
  
  @Autowired
  FacultyRepository facultyRepository;

  /* ========================================================== [ ADD FIELD ] ====================================================== */

  @PostMapping("/add")
  public ResponseEntity<DefaultFieldStatus> addField(@RequestBody AddFieldRequest request) {
    // In request: name, facultyId, [userEmail, userToken]
    // In response: if added (OK), field

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Input data validation
    if(request.getName().length() > 200 && request.getName().length() < 1) { throw new NotValidException("nameNotValid"); }

    // Check if faculty exists
    FacultyModel faculty = facultyRepository.findOneByIdAndUser(request.getFacultyId(), user);
    if(faculty == null) {
        throw new UserNotExists("facultyNotExists");
    }

    // Check if field already exists
    Boolean ifExists = fieldRepository.existsByNameAndFacultyAndUser(request.getName(), faculty, user);
    if(ifExists) {
        throw new UserExists("fieldAlreadyExists");
    }

    // Saving new faculty to database
    FieldModel field = new FieldModel(request.getName(), faculty, user);
    fieldRepository.saveAndFlush(field);

    return new ResponseEntity<>(new DefaultFieldStatus("fieldAdded", field), HttpStatus.CREATED);
  }

  /* ========================================================= [ EDIT FIELD ] ====================================================== */

  @PostMapping("/edit")
  public ResponseEntity<DefaultFieldStatus> editField(@RequestBody EditFieldRequest request) {
    // In request: id, name, [userEmail, userToken]
    // In response: if edited (OK), field

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if field user want to edit exists
    FieldModel field = fieldRepository.findOneByIdAndUser(request.getId(), user);
    if(field == null) {
        throw new UserNotExists("fieldNotExists");
    }

    // Input data validation
    if(request.getName().length() > 200 && request.getName().length() < 1) { throw new NotValidException("nameNotValid"); }

    // Check if faculty exists
    FacultyModel faculty = facultyRepository.findOneByIdAndUser(field.getFaculty().getId(), user);
    if(faculty == null) {
        throw new UserNotExists("facultyNotExists");
    }

    // Check if field already exists
    Boolean ifExists = fieldRepository.existsByNameAndFacultyAndUser(request.getName(), faculty, user);
    if(ifExists) {
        throw new UserExists("fieldAlreadyExists");
    }

    // Saving edited field to database
    field.setName(request.getName());
    fieldRepository.saveAndFlush(field);

    return new ResponseEntity<>(new DefaultFieldStatus("fieldEdited", field), HttpStatus.ACCEPTED);
  }

  /* ======================================================== [ DELETE FIELD ] ===================================================== */

  @PostMapping("/delete")
  public ResponseEntity<DefaultFieldStatus> deleteField(@RequestBody DeleteFieldRequest request) {
    // In request: id, [userEmail, userToken]
    // In response: if deleted (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for user's field to delete by id 
    FieldModel field = fieldRepository.findOneByIdAndUser(request.getId(), user);
    if(field == null) {
      throw new UserNotExists("fieldNotExists");
    }
    
    // Deleting field from database
    fieldRepository.delete(field);

    return new ResponseEntity<>(new DefaultFieldStatus("fieldDeleted"), HttpStatus.ACCEPTED);
  }

  /* =========================================================== [ GET FIELDS ] ======================================================= */

  @PostMapping("/get")
  public ResponseEntity<List<FieldModel>> getFields(@RequestBody CheckLoginRequest request) {
    // In request: type, [userEmail, userToken]
    // In response: list of fields

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if accout type is valid
    if(user.getType() != request.getType()) {
      throw new UserNotExists("typeError");
    }

    return new ResponseEntity<>(user.getFields(), HttpStatus.OK);
  }
}