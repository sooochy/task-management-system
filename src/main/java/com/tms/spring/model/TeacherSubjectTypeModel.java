package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import java.util.List;
import javax.persistence.*;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "teacher_subject_type")

public class TeacherSubjectTypeModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "teacher_id", nullable = false)
  private TeacherModel teacher;

  @ManyToOne
  @JsonIgnoreProperties({ "teacherSubjectTypes" })
  @JoinColumn(name = "subject_id", nullable = false)
  private SubjectModel subject;

  @ManyToOne
  @JoinColumn(name = "type_id", nullable = false)
  private TypeModel type;

  @JsonIgnore
  @OneToMany(orphanRemoval = true, mappedBy = "teacherSubjectType")
  private List<EventModel> events;

  @JsonIgnore
  @OneToMany(orphanRemoval = true, mappedBy = "teacherSubjectType")
  private List<PlanElementModel> planElements;

  @JsonIgnore
  @OneToMany(orphanRemoval = true, mappedBy = "teacherSubjectType")
  private List<HomeworkModel> homeworks;

  @JsonIgnore
  @OneToMany(orphanRemoval = true, mappedBy = "teacherSubjectType")
  private List<MaterialModel> materials;

  public TeacherSubjectTypeModel(TeacherModel teacher, SubjectModel subject, TypeModel type) {
    this.teacher = teacher;
    this.subject = subject;
    this.type = type;
  }
}
