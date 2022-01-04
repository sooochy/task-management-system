package com.tms.spring.request.SignIn;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.validator.routines.EmailValidator;

@Getter
@Setter
public class ForgotUserFirstStepRequest {
  private String email;
  private String language;

  public Boolean isValidEmail() {
    EmailValidator validator = EmailValidator.getInstance();
    return validator.isValid(getEmail());
  }
}