package com.tms.spring.request.Subjects;

import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
public class TeacherType {
  private Long id;
  private Long typeId;
  private Long teacherId;

  public Boolean equalsTeacherType(TeacherType obj) {
    return this.teacherId.equals(obj.getTeacherId()) && this.typeId.equals(obj.getTypeId());
  }
}
