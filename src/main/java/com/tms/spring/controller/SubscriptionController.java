package com.tms.spring.controller;

import java.time.LocalDateTime;
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

// Models
import com.tms.spring.model.UserModel;

// Repositories
import com.tms.spring.repository.UserRepository;

// Requests
import com.tms.spring.request.SignIn.CheckLoginRequest;

// Responses
import com.tms.spring.response.DefaultUserStatus;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/premium")
public class SubscriptionController {

  @Autowired
  UserRepository userRepository;

  /* ========================================================== [ BUY SUBSCRIPTION ] ======================================================= */

  @PostMapping("/buy")
  public ResponseEntity<DefaultUserStatus> buySupscription(@RequestBody CheckLoginRequest request) {
    // In request: [userEmail, userToken]
    // In response: if bought (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    LocalDateTime date = user.getSubExpirationDate();
    if(date != null) {
        user.setSubExpirationDate(date.plusDays(30));
    } else {
        user.setSubExpirationDate(LocalDateTime.now().plusDays(30));
    }

    userRepository.saveAndFlush(user);

    return new ResponseEntity<>(new DefaultUserStatus("subscriptionBought", user.getSubExpirationDate()), HttpStatus.CREATED);
  }

  /* ========================================================== [ GET SUBSCRIPTION ] ======================================================= */

  @PostMapping("/get")
  public ResponseEntity<DefaultUserStatus> getSupscription(@RequestBody CheckLoginRequest request) {
    // In request: type, [userEmail, userToken]
    // In response: if bought (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if accout type is valid
    if(user.getType() != request.getType()) {
        throw new UserNotExists("typeError");
    }

    if(user.getSubExpirationDate() == null || user.getSubExpirationDate().isBefore(LocalDateTime.now())) {
      return new ResponseEntity<>(new DefaultUserStatus("subscriptionExpired", null), HttpStatus.OK);
    }

    return new ResponseEntity<>(new DefaultUserStatus("subscriptionBought", user.getSubExpirationDate()), HttpStatus.OK);
  }
}