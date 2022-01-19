package com.tms.spring.email;

import com.tms.spring.email.SendEmailTLS;

public class ForgotUserFirstStepEmail {
    private final String email;
    private final String token;
    private final String language;

    public ForgotUserFirstStepEmail(String email, String token, String language) {
        this.email = email;
        this.token = token;
        this.language = language;
    }

    public Boolean sendForgotEmail() {
        SendEmailTLS emailToSend = new SendEmailTLS();

        String messageContent;
        String messageSubject;

        if(language.equals("pl")) {
            messageSubject = "[TMS] Zresetuj swoje hasło.";
            messageContent = "<body><h3>Witaj,</h3>" + 
            "Aby zresetować swoje hasło  <a href=\"http://tms.ts4ever.pl/forgot/secondstep/" + this.email + "/" + this.token + "\">w ten link</a>" +
            " i postępuj zgodnie z instrukcjami." + 
            
            "<br>Do zobaczenia!" +
            "<br>[TMS]</body>";
        }
        else {
            messageSubject = "[TMS] Reset your password.";
            messageContent = "<body><h3>Hello there,</h3>" + 
            "To reset your password, <a href=\"http://tms.ts4ever.pl/forgot/secondstep/" + this.email + "/" + this.token + "\">follow the link</a>" + 
            " and pursue given instructions." + 

            "<br>See ya!" +
            "<br>[TMS]</body>";
        }

        return emailToSend.send(email, messageSubject, messageContent);
    }
}