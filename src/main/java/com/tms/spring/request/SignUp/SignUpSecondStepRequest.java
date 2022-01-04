package com.tms.spring.request.SignUp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpSecondStepRequest {
  private String email;
  private String token;
  private String language;
}