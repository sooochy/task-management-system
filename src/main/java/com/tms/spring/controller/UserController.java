package com.tms.spring.controller;

import java.lang.String;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

// Exceptions
import com.tms.spring.exception.UserExists;
import com.tms.spring.exception.UserNotExists;
import com.tms.spring.exception.NotValidException;

// Models
import com.tms.spring.model.UserModel;

// Repositories
import com.tms.spring.repository.UserRepository;

// Requests
import com.tms.spring.request.Users.EditTypeRequest;
import com.tms.spring.request.Users.EditPasswordRequest;
import com.tms.spring.request.Users.DeleteAccountRequest;

// Responses
import com.tms.spring.response.DefaultUserStatus;

// Hashing
import com.tms.spring.hashing.HashingMachine;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/account")
public class UserController {

  @Autowired
  UserRepository userRepository;

  /* ========================================================== [ PASSWORD EDIT ] ======================================================= */

  @PostMapping("/password")
  public ResponseEntity<DefaultUserStatus> editPassword(@RequestBody EditPasswordRequest request) {
    // In request: id, oldPassword, newPassword [userEmail, userToken]
    // In response: if changed password (OK), user

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if old password is correct
    HashingMachine hashingMachine = new HashingMachine(request.getOldPassword());
    String hashedOldPassword = hashingMachine.hashingSha3();

    if(!hashedOldPassword.equals(user.getPassword())) { throw new UserNotExists("invalidOldPassword"); }

    // New password validation
    if(request.getNewPassword() == null || request.getNewPassword().length() < 6) { throw new NotValidException("invalidNewPassword"); }

    // Hashing user's new password with SHA-3 256 coding - this password will be replaced with old one in database
    hashingMachine = new HashingMachine(request.getNewPassword());
    String hashedNewPassword = hashingMachine.hashingSha3();

    // Adding user to 'users' entity (replacing with new password)
    user.setPassword(hashedNewPassword);
    userRepository.save(user);

    return new ResponseEntity<>(new DefaultUserStatus("passwordEdited", user), HttpStatus.CREATED);
  }

  /* ======================================================== [ ACCOUNT TYPE EDIT ] ===================================================== */

  @PostMapping("/type")
  public ResponseEntity<DefaultUserStatus> editAccount(@RequestBody EditTypeRequest request) {
    // In request: id, [userEmail, userToken]
    // In response: if changed type (OK), user

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    Short accountType = user.getType();
    Short student = 1, teacher = 2;
    if(accountType.equals(student)) { user.setType(teacher); }
    else { user.setType(student); }

    // Adding user to 'users' entity (replacing with new type)
    userRepository.save(user);

    return new ResponseEntity<>(new DefaultUserStatus("typeEdited", user), HttpStatus.CREATED);
  }

  /* ========================================================== [ ACCOUNT DELETE ] ======================================================= */

  @PostMapping("/delete")
  public ResponseEntity<DefaultUserStatus> deleteAccount(@RequestBody DeleteAccountRequest request) {
    // In request: id, [userEmail, userToken]
    // In response: if deleted (OK), editedUser

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Deleting user
    userRepository.delete(user);

    return new ResponseEntity<>(new DefaultUserStatus("userDeleted"), HttpStatus.CREATED);
  }
}