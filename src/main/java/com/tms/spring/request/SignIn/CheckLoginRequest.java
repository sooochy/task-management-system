package com.tms.spring.request.SignIn;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckLoginRequest {
  private Short type;
  private String userEmail;
  private String userToken;
}