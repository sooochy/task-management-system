package com.tms.spring.model;

import lombok.Setter;
import lombok.Getter;
import java.util.Base64;
import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import java.security.SecureRandom;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "forgot_user")
public class ForgotUserModel {
  @Id
  @Column(name = "user_id")
  private Long id;

  @Column(nullable = false)
  private String token; 

  @Column(nullable = false)
  private LocalDateTime date;

  @OneToOne
  @MapsId
  @JoinColumn(name = "user_id")
  private UserModel user;

  public ForgotUserModel(UserModel user) {
    this.user = user;
    this.date = LocalDateTime.now();
    createNewToken();
  }

  public void setDefaultDateTime() {
    this.date = LocalDateTime.now();
  }

  public void createNewToken() {
    SecureRandom secureRandom = new SecureRandom();
    Base64.Encoder base64Encoder = Base64.getUrlEncoder();
    byte[] randomBytes = new byte[40];
    secureRandom.nextBytes(randomBytes);
    String token = base64Encoder.encodeToString(randomBytes);
    this.token = token;
  }
}