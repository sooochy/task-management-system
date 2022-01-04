package com.tms.spring.request.Teachers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteTeacherRequest {
  private Long id;
  private String userEmail;
  private String userToken;
}