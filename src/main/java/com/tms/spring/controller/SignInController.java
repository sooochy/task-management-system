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
import com.tms.spring.request.SignIn.SignInRequest;
import com.tms.spring.request.SignIn.CheckLoginRequest;

// Responses
import com.tms.spring.response.DefaultSignInStatus;

// Hashing
import com.tms.spring.hashing.HashingMachine;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/signin")
public class SignInController {

  @Autowired
  UserRepository userRepository;

  /* ========================================================== [ SIGN IN ] ======================================================= */

  @PostMapping("")
  public ResponseEntity<DefaultSignInStatus> signIn(@RequestBody SignInRequest request) {
    // In request: email, password
    // In response: if logged in (OK), email, token, account type (1 - student, 2 - teacher)

    // Check if email exists in 'users'
    if(!userRepository.existsByEmail(request.getEmail())) {
        throw new UserNotExists("userNotExists"); // User with this email does not exists (entity: user)
    }

    // Hashing user's password with SHA-3 256 coding - this password is in database
    HashingMachine hashingMachine = new HashingMachine(request.getPassword());
    String hashedPassword = hashingMachine.hashingSha3();

    // Checking if password matches given email (if existingUser object == null, it means that password is wrong)
    UserModel existingUser = userRepository.findOneByEmailAndPassword(request.getEmail(), hashedPassword);
    if(existingUser == null) {
        throw new UserNotExists("wrongPassword");
    }

    // Creating user's private token with SHA-1 coding 
    String userToken = hashingMachine.createAuthToken(existingUser.getEmail(), existingUser.getPassword());

    return new ResponseEntity<>(new DefaultSignInStatus("userLoggedIn", existingUser.getEmail(), userToken, existingUser.getType()), HttpStatus.CREATED);
  }

  /* ======================================================= [ CHECK LOGIN ] ==================================================== */

  @PostMapping("/checklogin")
  public ResponseEntity<DefaultSignInStatus> checkLogin(@RequestBody CheckLoginRequest request) {
    // In request: type, email, token
    // In response: if exists (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if accout type is valid
    if(user.getType() != request.getType()) {
      throw new UserNotExists("typeError");
    }

    return new ResponseEntity<>(new DefaultSignInStatus("userExists"), HttpStatus.ACCEPTED);
  }
}