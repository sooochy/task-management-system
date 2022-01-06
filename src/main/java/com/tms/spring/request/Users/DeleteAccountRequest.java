package com.tms.spring.request.Users;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteAccountRequest {
  private Long id;
  private String userEmail;
  private String userToken;
}