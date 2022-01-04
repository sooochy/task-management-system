package com.tms.spring.request.SignUp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpThirdStepRequest {
  private String email;
  private String token;
  private String password;
  private Short type; // 1 - student, 2 - teacher
}