package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import java.util.List;
import javax.persistence.*;
import java.util.Collections;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "university")
public class UniversityModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String name;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;

  @JsonIgnoreProperties({ "university" })
  @OneToMany(orphanRemoval = true, mappedBy = "university")
  private List<FacultyModel> faculties;

  public UniversityModel(String name, UserModel user) {
    this.name = name;
    this.user = user;
    this.faculties = Collections.<FacultyModel>emptyList();
  }

  public UniversityModel(Long id, String name, UserModel user) {
    this.id = id;
    this.name = name;
    this.user = user;
    this.faculties = Collections.<FacultyModel>emptyList();
  }
}
