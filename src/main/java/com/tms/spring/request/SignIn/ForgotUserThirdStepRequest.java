package com.tms.spring.request.SignIn;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotUserThirdStepRequest {
  private String email;
  private String token;
  private String newPassword;
}