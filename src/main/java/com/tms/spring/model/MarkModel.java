package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import javax.persistence.*;
import java.time.LocalDateTime;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@Table(name = "mark")
public class MarkModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private Float mark;

  private LocalDateTime date;
  
  @Size(max = 2048)
  private String description;

  @OneToOne
  @JoinColumn(name = "event_id", referencedColumnName = "id")
  private EventModel event;

  @OneToOne
  @JoinColumn(name = "homework_id", referencedColumnName = "id")
  private HomeworkModel homework;

  @ManyToOne
  @JoinColumn(name = "teacherSubjectType_id")
  private TeacherSubjectTypeModel teacherSubjectType;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;

  public MarkModel() {}

  public MarkModel(HomeworkModel homework, UserModel user) {
    this.homework = homework;
    this.user = user;
  }

  public MarkModel(EventModel event, UserModel user) {
    this.event = event;
    this.user = user;
  }

  public MarkModel(Float mark, LocalDateTime date, String description, TeacherSubjectTypeModel teacherSubjectType, UserModel user) {
    this.mark = mark;
    this.date = date;
    this.description = description;
    this.teacherSubjectType = teacherSubjectType;
    this.user = user;
  }

  public MarkModel(Long id, Float mark, LocalDateTime date, String description, EventModel event, HomeworkModel homework, TeacherSubjectTypeModel teacherSubjectType, UserModel user) {
    this.id = id;
    this.mark = mark;
    this.date = date;
    this.description = description;
    this.event = event;
    this.homework = homework;
    this.teacherSubjectType = teacherSubjectType;
    this.user = user;
  }
}
