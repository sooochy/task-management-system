package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import java.util.List;
import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Size;
import com.tms.spring.hashing.HashingMachine;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user")
public class UserModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private Short type; // 1 - student, 2 - teacher

  private LocalDateTime subExpirationDate; // null if no subscription bought

  @Column(nullable = false)
  private Long uploadedFiles;

  @PrimaryKeyJoinColumn
  @OneToOne(orphanRemoval = true, mappedBy = "user")
  private ForgotUserModel forgotUser;

  @JsonIgnore
  @OneToMany(orphanRemoval = true, mappedBy = "user")
  private List<TeacherModel> teachers;

  @JsonIgnore
  @OneToMany(orphanRemoval = true, mappedBy = "user")
  private List<SubjectModel> subjects;

  @JsonIgnore
  @OneToMany(orphanRemoval = true, mappedBy = "user")
  private List<PlanElementModel> planElements;

  @JsonIgnore
  @OneToMany(orphanRemoval = true, mappedBy = "user")
  private List<UniversityModel> universities;

  @JsonIgnore
  @OneToMany(orphanRemoval = true, mappedBy = "user")
  private List<TypeModel> types;

  @JsonIgnore
  @OneToMany(orphanRemoval = true, mappedBy = "user")
  private List<EventModel> events;

  public UserModel(String email, String password, Short type) {
    this.email = email;
    this.password = password;
    this.type = type;
    this.subExpirationDate = null;
    this.uploadedFiles = 0L;
  }

  public Boolean checkUser(String token) {
    // Creating user's private token with SHA-1 coding -> hashingSha1(email +
    // "ujedxgtv5frjoegd4rt@#%&^#^(0agt5r4" + password) = user token
    HashingMachine hashingMachine = new HashingMachine();
    String hashedToken = hashingMachine.createAuthToken(getEmail(), getPassword());

    // Checking if it's a valid user by tokens comparision
    if (!token.equals(hashedToken)) {
      return false;
    }
    return true;
  }
}