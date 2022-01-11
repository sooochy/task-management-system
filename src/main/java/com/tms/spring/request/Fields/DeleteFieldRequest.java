package com.tms.spring.request.Fields;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteFieldRequest {
  private Long id;
  private String userEmail;
  private String userToken;
}