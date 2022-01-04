package com.tms.spring.exception;

import java.util.Date;
import org.springframework.http.HttpStatus;
import com.tms.spring.exception.UserExists;
import com.tms.spring.exception.ErrorMessage;
import com.tms.spring.exception.UserNotExists;
import org.springframework.http.ResponseEntity;
import com.tms.spring.exception.NotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(NotValidException.class)
  public ResponseEntity<ErrorMessage> notValidException(NotValidException ex, WebRequest request) {
    ErrorMessage message = new ErrorMessage(
        HttpStatus.CONFLICT.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));
    
    return new ResponseEntity<ErrorMessage>(message, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(UserNotExists.class)
  public ResponseEntity<ErrorMessage> usersNotExistsException(UserNotExists ex, WebRequest request) {
    ErrorMessage message = new ErrorMessage(
        HttpStatus.CONFLICT.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));
    
    return new ResponseEntity<ErrorMessage>(message, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(UserExists.class)
  public ResponseEntity<ErrorMessage> userExistsException(UserExists ex, WebRequest request) {
    ErrorMessage message = new ErrorMessage(
        HttpStatus.CONFLICT.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));
    
    return new ResponseEntity<ErrorMessage>(message, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorMessage> handleMaxSizeException(MaxUploadSizeExceededException ex, WebRequest request) {
    ErrorMessage message = new ErrorMessage(
      HttpStatus.CONFLICT.value(),
      new Date(),
      ex.getMessage(),
      request.getDescription(false));
  
    return new ResponseEntity<ErrorMessage>(message, HttpStatus.CONFLICT);
  }
}
