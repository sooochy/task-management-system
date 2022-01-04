package com.tms.spring.repository;

import com.tms.spring.model.RegistrationUserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationUserRepository extends JpaRepository<RegistrationUserModel, Long> {
  boolean existsByEmail(String email);

  RegistrationUserModel findOneByEmail(String email);
  RegistrationUserModel findOneByEmailAndToken(String email, String token);
}