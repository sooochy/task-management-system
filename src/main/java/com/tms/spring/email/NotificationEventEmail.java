package com.tms.spring.email;

import java.time.LocalDateTime;
import com.tms.spring.model.EventModel;
import com.tms.spring.email.SendEmailTLS;
import java.time.format.DateTimeFormatter;

public class NotificationEventEmail {
    private final String email;
    private final String language;
    private final EventModel event;

    public NotificationEventEmail(String email, String language, EventModel event) {
        this.email = email;
        this.language = language;
        this.event = event;
    }

    public Boolean sendEmail() {
        SendEmailTLS emailToSend = new SendEmailTLS();

        String messageContent;
        String messageSubject;
        LocalDateTime currentTime = LocalDateTime.now();

        String name = this.event.getName();
        LocalDateTime startDate = this.event.getStartDate();
        LocalDateTime endDate = this.event.getEndDate();

        if(language.equals("pl")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime dateTimeStart = startDate;
            LocalDateTime dateTimeEnd = endDate;

            String formattedDateTimeStart = dateTimeStart.format(formatter);
            String formattedDateTimeEnd = dateTimeEnd.format(formatter);

            Integer monthStart = startDate.getMonthValue();
            Integer monthEnd = endDate.getMonthValue();

            if(monthStart < 10) {
                if(startDate.isBefore(currentTime)) {
                    messageSubject = "[TMS] Twoje wydarzenie właśnie się rozpoczyna.";
                    messageContent = "<body><h3>Witaj,</h3>" + 
                    "Informujemy, że Twoje wydarzenie: '" + name + "' z datą rozpoczęcia " + startDate.getDayOfMonth() + ".0" + monthStart + "." + startDate.getYear() + " " + formattedDateTimeStart.substring(11, 13) + ":"  + formattedDateTimeStart.substring(14, 16) +
                    " trwać będzie do " + endDate.getDayOfMonth() + ".0" + monthEnd + "." + endDate.getYear() + " " + formattedDateTimeEnd.substring(11, 13) + ":"  + formattedDateTimeEnd.substring(14, 16) + "." +  

                    "<br>Pamiętaj o obecności!" +
                    "<br>[TMS]</body>";
                } else {
                    messageSubject = "[TMS] Zbliża się termin Twojego wydarzenia.";
                    messageContent = "<body><h3>Witaj,</h3>" + 
                    "Przypominamy o nadchodzącym wydarzeniu: '" + name + "', które rozpocznie się " + startDate.getDayOfMonth() + ".0" + monthStart + "." + startDate.getYear() + " o godzinie " + formattedDateTimeStart.substring(11, 13) + ":"  + formattedDateTimeStart.substring(14, 16) + " " +  
                    " i potrwa do " + endDate.getDayOfMonth() + ".0" + monthEnd + "." + endDate.getYear() + " " + formattedDateTimeEnd.substring(11, 13) + ":"  + formattedDateTimeEnd.substring(14, 16) + "." + 
                    "<br>Pamiętaj o obecności!" +
                    "<br>[TMS]</body>";
                }
            } else {
                if(startDate.isBefore(currentTime)) {
                    messageSubject = "[TMS] Twoje wydarzenie właśnie się rozpoczyna.";
                    messageContent = "<body><h3>Witaj,</h3>" + 
                    "Informujemy, że Twoje wydarzenie: '" + name + "' z datą rozpoczęcia " + startDate.getDayOfMonth() + "." + monthStart + "." + startDate.getYear() + " " + formattedDateTimeStart.substring(11, 13) + ":"  + formattedDateTimeStart.substring(14, 16) +
                    " trwać będzie do " + endDate.getDayOfMonth() + "." + monthEnd + "." + endDate.getYear() + " " + formattedDateTimeEnd.substring(11, 13) + ":"  + formattedDateTimeEnd.substring(14, 16) + "." +  

                    "<br>Pamiętaj o obecności!" +
                    "<br>[TMS]</body>";
                } else {
                    messageSubject = "[TMS] Zbliża się termin Twojego wydarzenia.";
                    messageContent = "<body><h3>Witaj,</h3>" + 
                    "Przypominamy o nadchodzącym wydarzeniu: '" + name + "', które rozpocznie się " + startDate.getDayOfMonth() + "." + monthStart + "." + startDate.getYear() + " o godzinie " + formattedDateTimeStart.substring(11, 13) + ":"  + formattedDateTimeStart.substring(14, 16) + " " +  
                    " i potrwa do " + endDate.getDayOfMonth() + "." + monthEnd + "." + endDate.getYear() + " " + formattedDateTimeEnd.substring(11, 13) + ":"  + formattedDateTimeEnd.substring(14, 16) + "." + 
                    "<br>Pamiętaj o obecności!" +
                    "<br>[TMS]</body>";
                }
            }
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
            LocalDateTime dateTimeStart = startDate;
            LocalDateTime dateTimeEnd = endDate;

            String formattedDateTimeStart = dateTimeStart.format(formatter);
            String formattedDateTimeEnd = dateTimeEnd.format(formatter);

            Integer monthStart = startDate.getMonthValue();
            Integer monthEnd = endDate.getMonthValue();

            if(startDate.isBefore(currentTime)) {
                messageSubject = "[TMS] Your event is about to begin.";
                messageContent = "<body><h3>Hello there,</h3>" + 
                "Please be advised that your event: '" + name + "' with the start date of " + startDate.getDayOfMonth() + " " + startDate.getMonth() + " " + startDate.getYear() + " " + formattedDateTimeStart.substring(11, 13) + ":"  + formattedDateTimeStart.substring(14, 16) + " " + formattedDateTimeStart.substring(17, 19) + " (now)" + 
                " will run until " + endDate.getDayOfMonth() + " " + endDate.getMonth() + " " + endDate.getYear() + " " + formattedDateTimeEnd.substring(11, 13) + ":"  + formattedDateTimeEnd.substring(14, 16) + " " + formattedDateTimeEnd.substring(17, 19) + "." +  

                "<br>Remember to be there!" +
                "<br>[TMS]</body>";
            } else {
                messageSubject = "[TMS] Your event is coming up.";
                messageContent = "<body><h3>Hello there,</h3>" + 
                "We remind you of the upcoming event: '" + name + "', which will start on " + startDate.getDayOfMonth() + " " + startDate.getMonth() + " " + startDate.getYear() + " at " + formattedDateTimeStart.substring(11, 13) + ":"  + formattedDateTimeStart.substring(14, 16) + " " + formattedDateTimeStart.substring(17, 19) + " " +  
                " and will last until " + endDate.getDayOfMonth() + " " + endDate.getMonth() + " " + endDate.getYear() + " " + formattedDateTimeEnd.substring(11, 13) + ":"  + formattedDateTimeEnd.substring(14, 16) + " " + formattedDateTimeEnd.substring(17, 19) + "." + 
                
                "<br>Remember to be there!" +
                "<br>[TMS]</body>";
            }
        }

        return emailToSend.send(email, messageSubject, messageContent);
    }
}
