package com.tms.spring.request.Subjects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditTypeRequest {
  private Long id;
  private String name;
  private String userEmail;
  private String userToken;
}