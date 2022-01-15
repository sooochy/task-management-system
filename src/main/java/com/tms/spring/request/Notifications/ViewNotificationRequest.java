package com.tms.spring.request.Notifications;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewNotificationRequest {
  private Long[] id;
  private String userEmail;
  private String userToken;
}