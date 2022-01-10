package com.tms.spring.request.Universities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditFacultyRequest {
  private Long id;  
  private String name;
  private String userEmail;
  private String userToken;
}