package com.tms.spring.request.Users;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditTypeRequest {
  private Long id;
  private String userEmail;
  private String userToken;
}