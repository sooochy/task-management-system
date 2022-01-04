package com.tms.spring.controller;

import java.lang.String;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
import com.tms.spring.model.ForgotUserModel;

// Repositories
import com.tms.spring.repository.UserRepository;
import com.tms.spring.repository.ForgotUserRepository;

// Requests
import com.tms.spring.request.SignIn.ForgotUserFirstStepRequest;
import com.tms.spring.request.SignIn.ForgotUserThirdStepRequest;
import com.tms.spring.request.SignIn.ForgotUserSecondStepRequest;

// Responses
import com.tms.spring.response.DefaultForgotStatus;

// Emails
import com.tms.spring.email.ForgotUserFirstStepEmail;

// Hashing
import com.tms.spring.hashing.HashingMachine;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/forgotpassword")
public class ForgotUserController {

  @Autowired
  UserRepository userRepository;

  @Autowired
  ForgotUserRepository forgotUserRepository;

  /* ========================================================== [ FORGOT PASSWORD FIRST STEP ] ======================================================= */

  @PostMapping("/firststep")
  public ResponseEntity<DefaultForgotStatus> enterEmail(@RequestBody ForgotUserFirstStepRequest request) {
    // In request: email, language
    // In response: if email sent (OK)

    // Language validation
    if(request.getLanguage() == null) {
      request.setLanguage("en");
    }

    // Email validation
    if(!request.isValidEmail()) {
      throw new NotValidException("invalidEmail");
    }

    // Check if email exists in 'users'
    UserModel userModel = userRepository.findOneByEmail(request.getEmail());
    if(userModel == null) {
      throw new UserNotExists("userNotExists"); // User with this email does not exists (entity: user)
    }

    // Checking if email exists in 'forgot_users' (resending email with overwriting registration user's token)
    ForgotUserModel existingUser = forgotUserRepository.findOneById(userModel.getId());
    if(existingUser != null) {
      existingUser.createNewToken();
      existingUser.setDefaultDateTime();
      forgotUserRepository.save(existingUser);

      // Sending email again to the same user
      ForgotUserFirstStepEmail email = new ForgotUserFirstStepEmail(userModel.getEmail(), existingUser.getToken(), request.getLanguage());
      email.sendForgotEmail();

      throw new UserExists("userExists");
    }

    // Creating new user with random token and same mail as given
    ForgotUserModel forgotUser = new ForgotUserModel(userModel);

    // Adding user to 'forgot_users'
    forgotUserRepository.save(forgotUser);

    // Sendning email to user with password reset form
    ForgotUserFirstStepEmail email = new ForgotUserFirstStepEmail(userModel.getEmail(), forgotUser.getToken(), request.getLanguage());
    email.sendForgotEmail();

    return new ResponseEntity<>(new DefaultForgotStatus("emailSent"), HttpStatus.ACCEPTED);
  }

  /* ========================================================== [ FORGOT PASSWORD SECOND STEP ] ======================================================= */

  @PostMapping("/secondstep")
  public ResponseEntity<DefaultForgotStatus> verifyEmail(@RequestBody ForgotUserSecondStepRequest request) {
    // In request: email, token, language
    // In response: if OK

    // Language validation
    if(request.getLanguage() == null) {
      request.setLanguage("en");
    }

    // Checking if email exists in 'users' 
    UserModel userModel = userRepository.findOneByEmail(request.getEmail());
    if(userModel == null) {
      throw new UserNotExists("userNotExists");
    }

    // If user currently does not exists in forgot_users (email may be, but with wrong token)
    ForgotUserModel existingUser = forgotUserRepository.findOneByIdAndToken(userModel.getId(), request.getToken());
    if(existingUser == null) {
      throw new UserNotExists("forgotUserNotExists");
    }

    // If an hour has passed since the email with password reset form was sent to user
    long difference = ChronoUnit.MINUTES.between(existingUser.getDate(), LocalDateTime.now());
    if(difference > 60) {
      existingUser.createNewToken();
      existingUser.setDefaultDateTime();
      forgotUserRepository.save(existingUser);

      // Sending email again to the same user automatically
      ForgotUserFirstStepEmail email = new ForgotUserFirstStepEmail(userModel.getEmail(), existingUser.getToken(), request.getLanguage());
      email.sendForgotEmail();
      
      throw new NotValidException("expiredRegistrationLink");  // OK 
    }

    return new ResponseEntity<>(new DefaultForgotStatus("existingAndValid"), HttpStatus.ACCEPTED);
  }

  /* ========================================================== [ FORGOT PASSWORD THIRD STEP ] ======================================================= */

  @PostMapping("/thirdstep")
  public ResponseEntity<DefaultForgotStatus> enterNewPassword(@RequestBody ForgotUserThirdStepRequest request) {
    // In request: email, token, new password
    // In response: if password set (OK)

    // New password validation
    if(request.getNewPassword().length() < 6) {
      throw new NotValidException("invalidPassword");
    }

    // Check if email exists in 'users'
    UserModel userModel = userRepository.findOneByEmail(request.getEmail());
    if(userModel == null) {
      throw new UserNotExists("userNotExists"); // User with this email already exists (entity: user)
    }
    
    // If user currently does not exists in register_users
    ForgotUserModel existingUser = forgotUserRepository.findOneByIdAndToken(userModel.getId(), request.getToken());
    if(existingUser == null) {
      throw new UserNotExists("forgotUserNotExists");
    }

    // Hashing user's new password with SHA-3 256 coding - this password will be replaced with old one in database
    HashingMachine hashingMachine = new HashingMachine(request.getNewPassword());
    String hashedNewPassword = hashingMachine.hashingSha3();

    // Adding user to 'users' entity (replacing with new password)
    userModel.setPassword(hashedNewPassword);
    userRepository.save(userModel);

    // Removing user from 'forgot_users' entity
    forgotUserRepository.delete(existingUser);

    return new ResponseEntity<>(new DefaultForgotStatus("newPasswordSet"), HttpStatus.ACCEPTED);
  }
}