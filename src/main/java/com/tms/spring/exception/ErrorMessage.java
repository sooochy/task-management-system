package com.tms.spring.exception;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter 
@Setter 
public class ErrorMessage {
  private Integer statusCode;
  private Date timestamp;
  private String message;
  private String description;

  public ErrorMessage(Integer statusCode, Date timestamp, String message, String description) {
    this.statusCode = statusCode;
    this.timestamp = timestamp;
    this.message = message;
    this.description = description;
  }
}