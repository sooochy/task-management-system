package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import java.util.List;
import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "faculty")
public class FacultyModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "university_id", nullable = false)
  private UniversityModel university;

  @OneToMany(mappedBy = "faculty")
  private List<FieldModel> fields;
}
