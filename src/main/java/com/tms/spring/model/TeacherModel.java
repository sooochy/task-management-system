package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import java.util.List;
import javax.persistence.*;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.validator.routines.EmailValidator;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "teacher")
public class TeacherModel {
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @Column(nullable = false)
  private String academicTitle; 

  private String email;
  
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;

  @JsonIgnore
  @OneToMany(orphanRemoval = true, mappedBy = "teacher")
  private List<TeacherSubjectTypeModel> teacherSubjectTypes;

  public TeacherModel(String firstName, String lastName, String academicTitle, String email, UserModel user) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.academicTitle = academicTitle;
    this.email = email;
    this.user = user;
  }
}
