package com.tms.spring.request.PlanElements;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeletePlanElementRequest {
  private Long id;
  private String userEmail;
  private String userToken;
}