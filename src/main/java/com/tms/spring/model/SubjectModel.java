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
@Table(name = "subject")
public class SubjectModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String name;

  @JsonIgnoreProperties({"subjects"})
  @ManyToOne
  @JoinColumn(name = "field_id")
  private FieldModel field;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;

  @OneToMany(orphanRemoval = true, mappedBy = "subject")
  @JsonIgnoreProperties({ "subject" })
  private List<TeacherSubjectTypeModel> teacherSubjectTypes;

  public SubjectModel(String name, UserModel user) {
    this.name = name;
    this.user = user;
  }

  public SubjectModel(String name, FieldModel field, UserModel user) {
    this.name = name;
    this.field = field;
    this.user = user;
  }

  public SubjectModel(Long id, String name, FieldModel field, UserModel user) {
    this.id = id;
    this.name = name;
    this.field = field;
    this.user = user;
  }
}
