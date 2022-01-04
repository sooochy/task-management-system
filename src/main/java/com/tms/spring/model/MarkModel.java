package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import javax.persistence.*;
import java.time.LocalDateTime;
import javax.validation.constraints.Size;

@Entity
@Getter
@Setter
@Table(name = "mark")
public class MarkModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private Float mark;

  @Column(nullable = false)
  private LocalDateTime date;
  
  @Size(max = 2048)
  private String description;

  @OneToOne
  @JoinColumn(name = "event_id", referencedColumnName = "id")
  private EventModel event;

  @OneToOne
  @JoinColumn(name = "homework_id", referencedColumnName = "id")
  private HomeworkModel homework;
}
