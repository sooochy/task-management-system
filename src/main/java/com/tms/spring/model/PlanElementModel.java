package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import javax.persistence.*;
import java.time.LocalTime;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "plan_element")
public class PlanElementModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = true)
  private String name;

  //@Size(max = 1)
  @Column(nullable = false)
  private Short day;

  @Column(nullable = false)
  private LocalTime startTime;

  @Column(nullable = false)
  private LocalTime endTime;

  //@Size(max = 1)
  @Column(nullable = false)
  private Short repetition;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;

  @ManyToOne
  @JoinColumn(name = "tst_id", nullable = true)
  private TeacherSubjectTypeModel teacherSubjectType;

  public PlanElementModel(String name, Short day, LocalTime startTime, LocalTime endTime, TeacherSubjectTypeModel teacherSubjectType, UserModel user, Short repetition) {
    this.name = name;
    this.day = day;
    this.startTime = startTime;
    this.endTime = endTime;
    this.teacherSubjectType = teacherSubjectType;
    this.user = user;
    this.repetition = repetition;
  }

  public PlanElementModel(Long id, String name, Short day, LocalTime startTime, LocalTime endTime, TeacherSubjectTypeModel teacherSubjectType, UserModel user, Short repetition) {
    this.id = id;
    this.name = name;
    this.day = day;
    this.startTime = startTime;
    this.endTime = endTime;
    this.teacherSubjectType = teacherSubjectType;
    this.user = user;
    this.repetition = repetition;
  }
}
