package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import java.util.List;
import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "field")
public class FieldModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "faculty_id", nullable = false)
  private FacultyModel faculty;

  @OneToMany(mappedBy = "field")
  private List<SubjectModel> subjects;
}