package com.tms.spring.email;

import java.time.LocalDateTime;
import com.tms.spring.email.SendEmailTLS;
import com.tms.spring.model.HomeworkModel;
import java.time.format.DateTimeFormatter;

public class NotificationEmail {
    private final String email;
    private final String language;
    private final HomeworkModel homework;

    public NotificationEmail(String email, String language, HomeworkModel homework) {
        this.email = email;
        this.language = language;
        this.homework = homework;
    }

    public Boolean sendEmail() {
        SendEmailTLS emailToSend = new SendEmailTLS();

        String messageContent;
        String messageSubject;
        LocalDateTime currentTime = LocalDateTime.now();

        String name = this.homework.getName();
        LocalDateTime deadline = this.homework.getDeadline();

        if(language.equals("pl")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime dateTime = deadline;
            String formattedDateTime = dateTime.format(formatter);
            Integer month = deadline.getMonthValue();

            if(month < 10) {
                if(deadline.isBefore(currentTime)) {
                    messageSubject = "[TMS] Termin wykonania zadania upłynął.";
                    messageContent = "<body><h3>Witaj,</h3>" + 
                    "Termin ukończenia zadania (" + deadline.getDayOfMonth() + ".0" + month + "." + deadline.getYear() + " " + formattedDateTime.substring(11, 13) + ":"  + formattedDateTime.substring(14, 16) + "): '" 
                    + name + "' właśnie upłynął." + 
                    
                    "<br>Pamiętaj o kolejnych!" +
                    "<br>[TMS]</body>";
                } else {
                    messageSubject = "[TMS] Zbliża się termin ukończenia zadania.";
                    messageContent = "<body><h3>Witaj,</h3>" + 
                    "Przypominamy, że termin ukończenia Twojego zadania: '" + name + 
                    "' upływa " + deadline.getDayOfMonth() + ".0" + month + "." + deadline.getYear() + " o godzinie " + formattedDateTime.substring(11, 13) + ":"  + formattedDateTime.substring(11, 13) + "." +  
                    
                    "<br>Powodzenia!" +
                    "<br>[TMS]</body>";
                }
            } else {
                if(deadline.isBefore(currentTime)) {
                    messageSubject = "[TMS] Termin wykonania zadania upłynął.";
                    messageContent = "<body><h3>Witaj,</h3>" + 
                    "Termin ukończenia zadania (" + deadline.getDayOfMonth() + "." + month + "." + deadline.getYear() + " " + deadline.getHour() + ":"  + deadline.getMinute() + "): '" + name + "' właśnie upłynął." + 
                    
                    "<br>Pamiętaj o kolejnych!" +
                    "<br>[TMS]</body>";
                } else {
                    messageSubject = "[TMS] Zbliża się termin ukończenia zadania.";
                    messageContent = "<body><h3>Witaj,</h3>" + 
                    "Przypominamy, że termin ukończenia Twojego zadania: '" + name + "' upływa " + deadline.getDayOfMonth() + "." + month + "." + deadline.getYear() + " o godzinie " + deadline.getHour() + ":"  + deadline.getMinute() + "." +  
                    
                    "<br>Powodzenia!" +
                    "<br>[TMS]</body>";
                }
            }
        }
        else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
            LocalDateTime dateTime = deadline;
            String formattedDateTime = dateTime.format(formatter);

            if(deadline.isBefore(currentTime)) {
                messageSubject = "[TMS] The deadline for the task has expired.";
                messageContent = "<body><h3>Hello there,</h3>" + 
                "Deadline (" + deadline.getDayOfMonth() + " " + deadline.getMonth() + " " + deadline.getYear() + 
                " at " + formattedDateTime.substring(11, 13) + ":"  + formattedDateTime.substring(11, 13) + " " + formattedDateTime.substring(17, 19) + ") " + 
                "for completing the task: " + "'" + name + "' has just expired." + 
                
                "<br>Remember about the next ones!" +
                "<br>[TMS]</body>";
            } else {
                messageSubject = "[TMS] The task is nearing your completion date.";
                messageContent = "<body><h3>Hello there,</h3>" + 
                "Just a reminder, that the deadline for completing your task: '" + name + 
                "' expires on " + deadline.getDayOfMonth() + " " + deadline.getMonth() + " " + deadline.getYear() + 
                " at " + formattedDateTime.substring(11, 13) + ":"  + formattedDateTime.substring(11, 13) + " " + formattedDateTime.substring(17, 19) + "." +  
                
                "<br>Godspeed!" +
                "<br>[TMS]</body>";
            }
        }

        return emailToSend.send(email, messageSubject, messageContent);
    }
}
