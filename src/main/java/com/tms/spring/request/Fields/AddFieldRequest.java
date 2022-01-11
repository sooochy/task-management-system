package com.tms.spring.request.Fields;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddFieldRequest {
  private String name;
  private Long facultyId;
  private String userEmail;
  private String userToken;
}