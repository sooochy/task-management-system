package com.tms.spring.request.Subjects;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;
import com.tms.spring.model.TypeModel;

@Getter
@Setter
public class AddTeacherSubjectRequest {
  private String name;
  private List<Type> types;
  private Long fieldId;
  private String userEmail;
  private String userToken;
}