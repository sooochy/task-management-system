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
@Table(name = "faculty")
public class FacultyModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String name;

  @JsonIgnoreProperties({ "faculties" })
  @ManyToOne
  @JoinColumn(name = "university_id", nullable = false)
  private UniversityModel university;

  @JsonIgnoreProperties({ "faculty" })
  @OneToMany(orphanRemoval = true, mappedBy = "faculty")
  private List<FieldModel> fields;

  public FacultyModel(String name, UniversityModel university) {
    this.name = name;
    this.university = university;
  }
}
