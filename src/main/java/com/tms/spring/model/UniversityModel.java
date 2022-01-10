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

  @OneToMany(mappedBy = "university")
  private List<FacultyModel> faculties;

  public UniversityModel(String name, UserModel user) {
    this.name = name;
    this.user = user;
  }

  public UniversityModel(Long id, String name, UserModel user) {
    this.id = id;
    this.name = name;
    this.user = user;
  }
}
