package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import java.util.List;
import javax.persistence.*;
import java.time.LocalDateTime;
import javax.validation.constraints.Size;

@Entity
@Getter
@Setter
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

  @OneToOne(mappedBy = "event")
  private MarkModel mark;

  @OneToMany(mappedBy = "event")
  private List<FileModel> files;

  @OneToMany(mappedBy = "event")
  private List<NotificationModel> notifications;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tst_id")
  private TeacherSubjectTypeModel teacherSubjectType;

  public EventModel(String name, String description, LocalDateTime startDate, LocalDateTime endDate, Boolean isMarked, TeacherSubjectTypeModel teacherSubjectType, UserModel user) {
    this.name = name;
    this.description = description;
    this.startDate = startDate;
    this.endDate = endDate;
    this.isMarked = isMarked;
    this.teacherSubjectType = teacherSubjectType;
    this.user = user;
  }
}
