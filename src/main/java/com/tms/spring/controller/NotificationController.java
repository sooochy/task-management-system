package com.tms.spring.controller;

import java.util.List;
import java.lang.String;
import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
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
import com.tms.spring.model.NotificationModel;
import com.tms.spring.response.NotificationList;

// Repositories
import com.tms.spring.repository.UserRepository;
import com.tms.spring.repository.NotificationRepository;

// Requests
import com.tms.spring.request.Notifications.ViewNotificationRequest;
import com.tms.spring.request.SignIn.CheckLoginRequest;

// Responses
import com.tms.spring.response.DefaultNotificationStatus;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

  @Autowired
  UserRepository userRepository;

  @Autowired
  NotificationRepository notificationRepository;

  /* ========================================================== [ GET NOTIFICATIONS ] ======================================================= */

  @PostMapping("/get")
  public ResponseEntity<List<NotificationList>> getNotifications(@RequestBody CheckLoginRequest request) {
    // In request: type, [userEmail, userToken]
    // In response: date the notification was displayed (one hour ago, 2 days ago etc.), homework deadline / event date, homework / event name

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if accout type is valid
    if(user.getType() != request.getType()) {
        throw new UserNotExists("typeError");
    }

    // Looking for all user's notifications sorted by isViewed (ascending) and AlertDate (descending)
    NotificationModel fullNotification = new NotificationModel();
    List<NotificationModel> userNotifications = notificationRepository.findAllByUserOrderByIsViewedAscAlertDateDesc(user);
    
    // Empty list to store notifications to show on dashboard (max 10 or all not viewed)
    List<NotificationList> notificationsList = new ArrayList<NotificationList>();

    for(Integer i = 0; i < userNotifications.size(); i++) {
        fullNotification = userNotifications.get(i);

        // Limit to 10 notifications or without limit if not viewed
        if(notificationsList.size() >= 10) {
            if(fullNotification.getIsViewed() == true) {
                break;
            }
        }

        if(fullNotification.getAlertDate().isBefore(LocalDateTime.now())) {
  
          // to get: id, homework/event name, homework/event deadline, alertDate, isViewed
          NotificationList notificationToShow = new NotificationList();
          notificationToShow.setId(fullNotification.getId());
  
          if(fullNotification.getHomework() != null) {
              notificationToShow.setName(fullNotification.getHomework().getName());
              notificationToShow.setDeadline(fullNotification.getHomework().getDeadline());
          } else {
              notificationToShow.setName(fullNotification.getEvent().getName());
              notificationToShow.setDeadline(fullNotification.getEvent().getStartDate());
          }
          notificationToShow.setAlertDate(fullNotification.getAlertDate());
          notificationToShow.setIsViewed(fullNotification.getIsViewed());
          notificationsList.add(notificationToShow);
        }
    }

    return new ResponseEntity<>(notificationsList, HttpStatus.CREATED);
  }

  /* =========================================================== [ VIEW NOTIFICATION ] ======================================================== */

  @PostMapping("/view")
  public ResponseEntity<DefaultNotificationStatus> viewNotifications(@RequestBody ViewNotificationRequest request) {
    // In request: id[], [userEmail, userToken]
    // In response: if viewed (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    for(Integer i = 0; i < request.getId().length; i++) {
        NotificationModel notification = notificationRepository.findOneByIdAndUser(request.getId()[i], user);
        if(notification == null) {
            throw new UserNotExists("notificationNotExists");
        }

        if(notification.getIsViewed() == false) {
            notification.setIsViewed(true);
            notificationRepository.saveAndFlush(notification);
        }
    }

    return new ResponseEntity<>(new DefaultNotificationStatus("notificationViewed"), HttpStatus.OK);
  }
}