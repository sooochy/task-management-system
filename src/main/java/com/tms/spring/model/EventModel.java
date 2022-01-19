package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import java.util.List;
import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "event")
public class EventModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Size(max = 2048)
  private String description;

  @Column(nullable = false)
  private LocalDateTime startDate;

  @Column(nullable = false)
  private LocalDateTime endDate;

  @Column(nullable = false)
  private Boolean isMarked;

  @JsonIgnore
  @OneToOne(orphanRemoval = true, mappedBy = "event")
  private MarkModel mark;

  @OneToMany(orphanRemoval = true, mappedBy = "event")
  @JsonIgnoreProperties({ "type", "data", "material", "homework", "event" })
  private List<FileModel> files;

  @OneToMany(orphanRemoval = true, mappedBy = "event")
  @JsonIgnoreProperties({ "homework", "event" })
  private List<NotificationModel> notifications;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;

  @ManyToOne
  @JoinColumn(name = "tst_id")
  private TeacherSubjectTypeModel teacherSubjectType;

  public EventModel(String name, String description, LocalDateTime startDate, LocalDateTime endDate, Boolean isMarked,
      TeacherSubjectTypeModel teacherSubjectType, UserModel user) {
    this.name = name;
    this.description = description;
    this.startDate = startDate;
    this.endDate = endDate;
    this.isMarked = isMarked;
    this.teacherSubjectType = teacherSubjectType;
    this.user = user;
  }

  public EventModel(Long id, String name, String description, LocalDateTime startDate, LocalDateTime endDate,
      Boolean isMarked, TeacherSubjectTypeModel teacherSubjectType, UserModel user) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.startDate = startDate;
    this.endDate = endDate;
    this.isMarked = isMarked;
    this.teacherSubjectType = teacherSubjectType;
    this.user = user;
  }
}
