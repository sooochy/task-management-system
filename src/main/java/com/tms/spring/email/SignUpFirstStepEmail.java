package com.tms.spring.email;

import com.tms.spring.email.SendEmailTLS;

public class SignUpFirstStepEmail {
    private final String email;
    private final String token;
    private final String language;

    public SignUpFirstStepEmail(String email, String token, String language) {
        this.email = email;
        this.token = token;
        this.language = language;
    }

    public Boolean sendEmail() {
        SendEmailTLS emailToSend = new SendEmailTLS();

        String messageContent;
        String messageSubject;

        if(language.equals("pl")) {
            messageSubject = "[TMS] Zweryfikuj swój email.";
            messageContent = "<body><h3>Witaj,</h3>" + 
            "Aby aktywować swoje konto i przejść do kolejnego etapu rejestracji przejdź <a href=\"http://tms.ts4ever.pl/signup/secondstep/" + this.email + "/" + this.token + "\">w ten link</a>" +
            " i postępuj zgodnie z instrukcjami." + 
            
            "<br>Do zobaczenia!" +
            "<br>[TMS]</body>";
        }
        else {
            messageSubject = "[TMS] Verify your email.";
            messageContent = "<body><h3>Hello there,</h3>" + 
            "To activate your account and go to the next stage of registration, <a href=\"http://tms.ts4ever.pl/signup/secondstep/" + this.email + "/" + this.token + "\">follow the link</a>" + 
            " and pursue given instructions." + 

            "<br>See ya!" +
            "<br>[TMS]</body>";
        }

        return emailToSend.send(email, messageSubject, messageContent);
    }
}


