package com.tms.spring.controller;

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
import com.tms.spring.model.RegistrationUserModel;

// Repositories
import com.tms.spring.repository.UserRepository;
import com.tms.spring.repository.RegistrationUserRepository;

// Requests
import com.tms.spring.request.SignUp.SignUpFirstStepRequest;
import com.tms.spring.request.SignUp.SignUpSecondStepRequest;
import com.tms.spring.request.SignUp.SignUpThirdStepRequest;

// Responses
import com.tms.spring.response.DefaultSignUpStatus;

// Emails
import com.tms.spring.email.SignUpFirstStepEmail;

// Hashing 
import com.tms.spring.hashing.HashingMachine;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/signup")
public class SignUpController {

  @Autowired
  UserRepository userRepository;

  @Autowired
  RegistrationUserRepository registrationUserRepository;

  /* ========================================================== [ REGISTRATION FIRST STEP ] ======================================================= */

  @PostMapping("/firststep")
  public ResponseEntity<DefaultSignUpStatus> createRegistrationUser(@RequestBody SignUpFirstStepRequest request) {
    // In request: email, language
    // in response: if registration user added (OK)

    // Language validation
    if(request.getLanguage() == null) {
      request.setLanguage("en");
    }

    // Email validation
    if(!request.isValidEmail()) {
      throw new NotValidException("invalidEmail");
    }

    // Check if email exists in 'users'
    if(userRepository.existsByEmail(request.getEmail())) {
      throw new UserExists("userExists"); // User with this email already exists (entity: user)
    }

    // Check if email exists in 'registration_users' (resending email with overwriting registration user's token)
    if(registrationUserRepository.existsByEmail(request.getEmail())) {
      RegistrationUserModel existingUser = registrationUserRepository.findOneByEmail(request.getEmail());
      existingUser.createNewToken();
      existingUser.setDefaultDateTime();
      registrationUserRepository.save(existingUser);

      // Sending email again to the same user
      SignUpFirstStepEmail email = new SignUpFirstStepEmail(existingUser.getEmail(), existingUser.getToken(), request.getLanguage());
      email.sendEmail();

      throw new UserExists("registrationUserExists"); // User with this email already exists (entity: registration_users)
    }

    // Adding user to "temporary" 'registration_users' entity (only for 1 hour from the time the email was sent)
    RegistrationUserModel newUser = new RegistrationUserModel(request.getEmail());
    registrationUserRepository.save(newUser);

    // Send email to user
    SignUpFirstStepEmail email = new SignUpFirstStepEmail(newUser.getEmail(), newUser.getToken(), request.getLanguage());
    email.sendEmail();
      
    return new ResponseEntity<>(new DefaultSignUpStatus("registrationUserAdded"), HttpStatus.CREATED);
  }

  /* ========================================================== [ REGISTRATION SECOND STEP ] ======================================================= */

  @PostMapping("/secondstep")
  public ResponseEntity<DefaultSignUpStatus> verifyUserEmail(@RequestBody SignUpSecondStepRequest request) {
    // In request: email, token, language
    // In response: if existing and valid (OK)

    // Language validation
    if(request.getLanguage() == null) {
      request.setLanguage("en");
    }

    // Check if email exists in 'users'
    if(userRepository.existsByEmail(request.getEmail())) {
      throw new UserExists("userExists"); // User with this email already exists (entity: user)
    }

    // If user currently does not exists in register_users
    RegistrationUserModel existingUser = registrationUserRepository.findOneByEmailAndToken(request.getEmail(), request.getToken());
    if(existingUser == null) {
      throw new UserNotExists("registrationUserNotExists");
    }

    // If an hour has passed since the email with confirmation was sent
    long difference = ChronoUnit.MINUTES.between(existingUser.getDate(), LocalDateTime.now());

    if(difference > 60) {
      existingUser.createNewToken();
      existingUser.setDefaultDateTime();
      registrationUserRepository.save(existingUser);

      // Sending email again to the same user automatically
      SignUpFirstStepEmail email = new SignUpFirstStepEmail(existingUser.getEmail(), existingUser.getToken(), request.getLanguage());
      email.sendEmail();
      
      throw new NotValidException("expiredRegistrationLink");  // OK 
    }
    
    return new ResponseEntity<>(new DefaultSignUpStatus("existingAndValid"), HttpStatus.ACCEPTED);
  }

 /* =========================================================== [ REGISTRATION THIRD STEP ] ======================================================== */
 
  @PostMapping("/thirdstep")
  public ResponseEntity<DefaultSignUpStatus> completeUserDetails(@RequestBody SignUpThirdStepRequest request) {
    // In request: email, token, password, account type
    // In response: if user added (OK)

    // Password validation
    if(request.getPassword().length() < 6) {
      throw new NotValidException("invalidPassword");
    }

    // Account type validation
    if(!(request.getType() == 1 || request.getType() == 2)) {  
      throw new NotValidException("invalidType");
    }

    // Check if email exists in 'users'
    if(userRepository.existsByEmail(request.getEmail())) {
      throw new UserExists("userExists"); // User with this email already exists (entity: user)
    }

    // If user currently does not exists in register_users
    RegistrationUserModel existingUser = registrationUserRepository.findOneByEmailAndToken(request.getEmail(), request.getToken());
    if(existingUser == null) {
      throw new UserNotExists("registrationUserNotExists");
    }

    // Hashing user's password with SHA-3 256 coding - this password is in database
    HashingMachine hashingMachine = new HashingMachine(request.getPassword());
    String hashedPassword = hashingMachine.hashingSha3();

    // Adding user to 'users' entity 
    UserModel newUser = new UserModel(request.getEmail(), hashedPassword, request.getType());
    userRepository.save(newUser);

    // Removing already registered user from 'registration_users' entity
    registrationUserRepository.delete(existingUser);

    return new ResponseEntity<>(new DefaultSignUpStatus("userAdded"), HttpStatus.CREATED);
  }
}