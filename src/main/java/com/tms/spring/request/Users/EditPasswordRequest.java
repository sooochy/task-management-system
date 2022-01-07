package com.tms.spring.request.Users;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditPasswordRequest {
  private Long id;
  private String oldPassword;
  private String newPassword;
  private String userEmail;
  private String userToken;
}