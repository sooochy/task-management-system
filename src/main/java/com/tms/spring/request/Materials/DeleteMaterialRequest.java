package com.tms.spring.request.Materials;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteMaterialRequest {
  private Long id;
  private String userEmail;
  private String userToken;
}