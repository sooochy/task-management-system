package com.tms.spring.request.Teachers;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.validator.routines.EmailValidator;

@Getter
@Setter
public class AddTeacherRequest {
  private String firstName;
  private String lastName;
  private String academicTitle;
  private String email;
  private String userEmail;
  private String userToken;

  public Boolean isValidEmail() {
    EmailValidator validator = EmailValidator.getInstance();
    return validator.isValid(getEmail());
  }
}