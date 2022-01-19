package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
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
  @JoinColumn(name = "tst_id", nullable = false)
  private TeacherSubjectTypeModel teacherSubjectType;

  public MarkModel(HomeworkModel homework, TeacherSubjectTypeModel teacherSubjectType) {
    this.homework = homework;
    this.teacherSubjectType = teacherSubjectType;
  }

  public MarkModel(EventModel event, TeacherSubjectTypeModel teacherSubjectType) {
    this.event = event;
    this.teacherSubjectType = teacherSubjectType;
  }

  public MarkModel(Float mark, LocalDateTime date, String description, TeacherSubjectTypeModel teacherSubjectType) {
    this.mark = mark;
    this.date = date;
    this.description = description;
    this.teacherSubjectType = teacherSubjectType;
  }

  public MarkModel(Long id, Float mark, LocalDateTime date, String description, EventModel event,
      HomeworkModel homework, TeacherSubjectTypeModel teacherSubjectType) {
    this.id = id;
    this.mark = mark;
    this.date = date;
    this.description = description;
    this.event = event;
    this.homework = homework;
    this.teacherSubjectType = teacherSubjectType;
  }
}
