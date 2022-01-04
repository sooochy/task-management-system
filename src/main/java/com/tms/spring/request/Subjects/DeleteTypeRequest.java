package com.tms.spring.request.Subjects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteTypeRequest {
  private Long id;
  private String userEmail;
  private String userToken;
}