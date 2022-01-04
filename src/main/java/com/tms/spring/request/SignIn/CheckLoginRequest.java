package com.tms.spring.request.SignIn;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckLoginRequest {
  private String userEmail;
  private String userToken;
}