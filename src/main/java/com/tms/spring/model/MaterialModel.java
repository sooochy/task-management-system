package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import java.util.List;
import javax.persistence.*;
import java.time.LocalDate;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Entity
@Getter
@Setter
@Table(name = "material")
public class MaterialModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Size(max = 2048)
  private String description;

  @Column(nullable = false)
  private LocalDate date;

  @JsonIgnoreProperties({ "type", "data", "material", "homework", "event" })
  @OneToMany(orphanRemoval = true, mappedBy = "material")
  private List<FileModel> files;

  @ManyToOne
  @JoinColumn(name = "tst_id", nullable = false)
  private TeacherSubjectTypeModel teacherSubjectType;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;

  public MaterialModel() {}

  public MaterialModel(String name, String description, LocalDate date, TeacherSubjectTypeModel teacherSubjectType, UserModel user) {
    this.name = name;
    this.description = description;
    this.date = date;
    this.teacherSubjectType = teacherSubjectType;
    this.user = user;
  }

  public MaterialModel(Long id, String name, String description, LocalDate date, TeacherSubjectTypeModel teacherSubjectType, UserModel user) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.date = date;
    this.teacherSubjectType = teacherSubjectType;
    this.user = user;
  }
}
