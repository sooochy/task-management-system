package com.tms.spring.email;

import javax.mail.*;
import java.util.Properties;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendEmailTLS {
    public Boolean send(String email, String subject, String body) {
      String username = "taskmanagementsystempk@gmail.com";
      String password = "dbggqvjtaeeciawc";

      Properties prop = new Properties();
      prop.put("mail.smtp.host", "smtp.gmail.com");
      prop.put("mail.smtp.port", "587");
      prop.put("mail.smtp.auth", "true");
      prop.put("mail.smtp.starttls.enable", "true");

      Session session = Session.getInstance(prop,
        new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        }
      );

      try {
        Message message = new MimeMessage(session);
        System.setProperty("mail.mime.charset", "UTF-8");
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        message.setSubject(subject);
        message.setContent(body, "text/html; charset=UTF-8");

        Transport.send(message);

        return true;
      } catch (MessagingException e) {
          e.printStackTrace();
      }
      return false;
    }
}