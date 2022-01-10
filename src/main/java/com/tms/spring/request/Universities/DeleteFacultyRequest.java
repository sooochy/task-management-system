package com.tms.spring.request.Universities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteFacultyRequest {
  private Long id;
  private String userEmail;
  private String userToken;
}