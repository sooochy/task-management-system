package com.tms.spring.request.Universities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddFacultyRequest {
  private String name;
  private Long universityId;
  private String userEmail;
  private String userToken;
}