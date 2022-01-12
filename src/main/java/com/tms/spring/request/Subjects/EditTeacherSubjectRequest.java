package com.tms.spring.request.Subjects;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
public class EditTeacherSubjectRequest {
  private Long id;
  private String name;
  private List<Type> types;
  private Long fieldId;
  private String userEmail;
  private String userToken;
}