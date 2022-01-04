package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import java.util.List;
import javax.persistence.*;
import java.time.LocalDateTime;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter
@Setter
@Table(name = "homework")
public class HomeworkModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Size(max = 2048)
  private String description;

  @Column(nullable = false)
  private LocalDateTime deadline;

  @Column(nullable = false)
  private Long estimatedTime;

  @Column(nullable = false)
  private LocalDateTime date;

  @Column(nullable = false)
  private Boolean isMarked;

  @Column(nullable = false)
  private Boolean isDone;

  @JsonIgnore
  @OneToOne(mappedBy = "homework")
  private MarkModel mark;

  @OneToMany(orphanRemoval = true, mappedBy = "homework")
  @JsonIgnoreProperties({ "type", "data", "material", "homework", "event" })
  private List<FileModel> files;

  @OneToMany(orphanRemoval = true, mappedBy = "homework")
  @JsonIgnoreProperties({ "homework", "event" })
  private List<NotificationModel> notifications;

  @ManyToOne
  @JoinColumn(name = "tst_id", nullable = false)
  private TeacherSubjectTypeModel teacherSubjectType;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;

  public HomeworkModel() {}

  public HomeworkModel(String name, String description, LocalDateTime deadline, Long estimatedTime, LocalDateTime date, 
      Boolean isMarked, TeacherSubjectTypeModel teacherSubjectType, UserModel user) {
    this.name = name;
    this.description = description;
    this.deadline = deadline;
    this.estimatedTime = estimatedTime;
    this.date = date;
    this.isMarked = isMarked;
    this.isDone = false;
    this.teacherSubjectType = teacherSubjectType;
    this.user = user;
  }

  public HomeworkModel(Long id, String name, String description, LocalDateTime deadline, Long estimatedTime, LocalDateTime date, 
      Boolean isMarked, TeacherSubjectTypeModel teacherSubjectType, UserModel user) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.deadline = deadline;
    this.estimatedTime = estimatedTime;
    this.date = date;
    this.isMarked = isMarked;
    this.isDone = false;
    this.teacherSubjectType = teacherSubjectType;
    this.user = user;
  }
}
