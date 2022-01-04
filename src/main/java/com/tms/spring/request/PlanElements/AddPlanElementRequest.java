package com.tms.spring.request.PlanElements;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Getter
@Setter
public class AddPlanElementRequest {
  private String name;
  private Short day;
  private LocalTime startTime;
  private LocalTime endTime;
  private Long tstId;
  private Short repetition;
  private String userEmail;
  private String userToken;
}