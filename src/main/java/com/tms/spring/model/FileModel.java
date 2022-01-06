package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Getter
@Setter
@Table(name = "file")
public class FileModel {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  private String id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String type;

  @Lob
  private byte[] data;

  @Column(nullable = false)
  private Long size;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "material_id")
  private MaterialModel material;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "homework_id")
  private HomeworkModel homework;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id")
  private EventModel event;

  public FileModel() {
  }

  public FileModel(String name, String type, byte[] data, MaterialModel material, Long size) {
    this.name = name;
    this.type = type;
    this.data = data;
    this.material = material;
    this.homework = null;
    this.event = null;
    this.size = size;
  }

  public FileModel(String name, String type, byte[] data, HomeworkModel homework, Long size) {
    this.name = name;
    this.type = type;
    this.data = data;
    this.material = null;
    this.homework = homework;
    this.event = null;
    this.size = size;
  }

  public FileModel(String name, String type, byte[] data, EventModel event, Long size) {
    this.name = name;
    this.type = type;
    this.data = data;
    this.material = null;
    this.homework = null;
    this.event = event;
    this.size = size;
  }
}
