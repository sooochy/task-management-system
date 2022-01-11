package com.tms.spring.request.Fields;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditFieldRequest {
  private Long id;
  private String name;
  private String userEmail;
  private String userToken;
}