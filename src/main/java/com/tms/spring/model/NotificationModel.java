package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
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

  @ManyToOne
  @JoinColumn(name = "event_id")
  @JsonIgnoreProperties({ "notifications" })
  private EventModel event;
  
  @ManyToOne
  @JoinColumn(name = "homework_id")
  @JsonIgnoreProperties({ "notifications" })
  private HomeworkModel homework;

  public NotificationModel(LocalDateTime alertDate, String language, HomeworkModel homework) {
    this.alertDate = alertDate;
    this.language = language;
    this.homework = homework;
    this.isViewed = false;
    this.isSent = false;
  }

  public NotificationModel(LocalDateTime alertDate, String language, EventModel event) {
    this.alertDate = alertDate;
    this.language = language;
    this.event = event;
    this.isViewed = false;
    this.isSent = false;
  }
}
