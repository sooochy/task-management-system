package com.tms.spring.request.Subjects;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
public class DeleteUserSubjectRequest {
  private Long id;
  private String userEmail;
  private String userToken;
}