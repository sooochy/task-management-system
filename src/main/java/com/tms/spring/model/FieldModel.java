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
@Table(name = "field")
public class FieldModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String name;

  @JsonIgnoreProperties({ "fields" })
  @ManyToOne
  @JoinColumn(name = "faculty_id", nullable = false)
  private FacultyModel faculty;

  @JsonIgnore
  @OneToMany(orphanRemoval = true, mappedBy = "field")
  private List<SubjectModel> subjects;

  public FieldModel(String name, FacultyModel faculty) {
    this.name = name;
    this.faculty = faculty;
  }
}
