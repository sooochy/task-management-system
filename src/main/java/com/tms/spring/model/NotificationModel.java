package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;

import java.util.Date;
import javax.persistence.*;
import java.time.LocalDateTime;
import javax.validation.constraints.Size;

@Entity
@Getter
@Setter
@Table(name = "notification")
public class NotificationModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private LocalDateTime alertDate;

  @Column(nullable = false)
  private Boolean isViewed;

  @Column(nullable = false)
  private Boolean isSent;

  @Column(nullable = false)
  private String language;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id")
  private EventModel event;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "homework_id")
  private HomeworkModel homework;

  public NotificationModel() {}

  public NotificationModel(LocalDateTime alertDate, String language, HomeworkModel homework) {
    this.alertDate = alertDate;
    this.language = language;
    this.homework = homework;
    this.isViewed = false;
    this.isSent = false;
  }
}
