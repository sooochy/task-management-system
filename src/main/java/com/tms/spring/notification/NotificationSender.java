package com.tms.spring.notification;

import java.util.List;
import java.util.Date;
import org.slf4j.Logger;
import java.util.Collections;
import java.time.LocalDateTime;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import com.tms.spring.model.UserModel;
import com.tms.spring.model.EventModel;
import com.tms.spring.model.HomeworkModel;
import com.tms.spring.model.NotificationModel;
import com.tms.spring.exception.UserNotExists;
import org.springframework.stereotype.Component;
import com.tms.spring.repository.UserRepository;
import com.tms.spring.repository.EventRepository;
import com.tms.spring.email.NotificationEventEmail;
import com.tms.spring.repository.HomeworkRepository;
import com.tms.spring.email.NotificationHomeworkEmail;
import com.tms.spring.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class NotificationSender {

    // If the current date and time is equal to or greater than the alertDate time in notification:
    //  -> send an email to the user with the message about deadline and remaining time of task with its name
    //  -> set isSent value to true to avoid multiple mails

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;
    
    @Autowired
    HomeworkRepository homeworkRepository;
    
    @Autowired
    NotificationRepository notificationRepository;

    // Task is called every 60 seconds
	@Scheduled(fixedRate = 60000)
	public void checkAlertDate() {
        UserModel user;
        EventModel event;
        HomeworkModel homework;
        NotificationModel notification;
        NotificationEventEmail eventEmail;
        NotificationHomeworkEmail homeworkEmail;
        List<NotificationModel> notifications = Collections.<NotificationModel>emptyList();

        // Looking for all notifications in database
        notifications = notificationRepository.findAllByIsSentAndBeforeNow(false);

        for(Integer i = 0; i < notifications.size(); i++) {
            notification = notifications.get(i);
            notification.setIsSent(true);

            if(notification.getHomework() != null) {
                homework = homeworkRepository.findOneById(notification.getHomework().getId());
                user = userRepository.findOneById(homeworkRepository.getUserId(homework.getId()));

                if(homework.getIsDone() == false) {
                    homeworkEmail = new NotificationHomeworkEmail(user.getEmail(), notification.getLanguage(), homework);
                    homeworkEmail.sendEmail();
                }
            } else if(notification.getEvent() != null) {
                event = eventRepository.findOneById(notification.getEvent().getId());

                eventEmail = new NotificationEventEmail(event.getUser().getEmail(), notification.getLanguage(), event);
                eventEmail.sendEmail();
            }

            notificationRepository.saveAndFlush(notification);
        }


        /* // Checking whether the notification e-mail has already been sent or is waiting in the queue
        for(Integer i = 0; i < notifications.size(); i++) {
            notification = notifications.get(i);

            if(notification.getAlertDate().isBefore(currentTime) && notification.getHomework() != null  && notification.getEvent() == null && notification.getIsSent().equals(false)) {             
                // Marking the notification as sent
                notification.setIsSent(true);

                // Looking for user's notification email and language through homework table
                homework = homeworkRepository.findOneById(notification.getHomework().getId());
                if(homework == null) { throw new UserNotExists("homeworkNotExists"); }

                // Exclusion of the possibility of sending an e-mail to completed tasks
                if(homework.getIsDone()) { break; }

                // Checking to which user the notification should be sent
                user = homeworkRepository.getUserById(homework.getId());
                if(user == null) { throw new UserNotExists("userNotExists"); }

                // Sending email to user
                homeworkEmail = new NotificationHomeworkEmail(user.getEmail(), notification.getLanguage(), homework);
                homeworkEmail.sendEmail();

                // Saving notification as sent
                notificationRepository.saveAndFlush(notification);
            } else if(notification.getAlertDate().isBefore(currentTime) && notification.getHomework() == null  && notification.getEvent() != null && notification.getIsSent().equals(false)) {
                // Marking the notification as sent
                notification.setIsSent(true);

                // Looking for user's notification email and language through event table
                event = eventRepository.findOneById(notification.getEvent().getId());
                if(event == null) { throw new UserNotExists("eventNotExists"); }

                // Checking to which user the notification should be sent
                user = eventRepository.getUserById(event.getId());
                if(user == null) { throw new UserNotExists("userNotExists"); }

                // Sending email to user
                eventEmail = new NotificationEventEmail(user.getEmail(), notification.getLanguage(), event);
                eventEmail.sendEmail();

                // Saving notification as sent
                notificationRepository.saveAndFlush(notification);
            }
        } */
	}
} 