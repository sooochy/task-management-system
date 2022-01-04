package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import java.util.List;
import javax.persistence.*;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "type")
public class TypeModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String name;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;

  @JsonIgnore
  @OneToMany(orphanRemoval = true, mappedBy = "type")
  private List<TeacherSubjectTypeModel> teacherSubjectTypes;

  public TypeModel(String name, UserModel user) {
    this.name = name;
    this.user = user;
  };
}
