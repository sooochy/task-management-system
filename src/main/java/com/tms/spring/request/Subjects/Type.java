package com.tms.spring.request.Subjects;

import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
public class Type {
  private Long id;
  private Long typeId;

  public Boolean equalsType(Type obj) {
    return this.typeId.equals(obj.getTypeId());
  }
}
