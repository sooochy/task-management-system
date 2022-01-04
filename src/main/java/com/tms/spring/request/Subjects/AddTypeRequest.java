package com.tms.spring.request.Subjects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddTypeRequest {
  private String name;
  private String userEmail;
  private String userToken;
}