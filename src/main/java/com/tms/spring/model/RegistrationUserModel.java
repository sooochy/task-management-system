package com.tms.spring.model;

import lombok.Getter;
import lombok.Setter;
import java.util.Base64;
import javax.persistence.*; 
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import java.security.SecureRandom;

@Entity 
@Getter 
@Setter
@NoArgsConstructor
@Table(name = "registration_user")
public class RegistrationUserModel {
  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;

	@Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String token;

  @Column(nullable = false)
  private LocalDateTime date;

  public RegistrationUserModel(String email) {
		this.email = email;
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