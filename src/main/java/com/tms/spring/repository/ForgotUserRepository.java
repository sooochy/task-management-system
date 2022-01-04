package com.tms.spring.repository;

import com.tms.spring.model.ForgotUserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgotUserRepository extends JpaRepository<ForgotUserModel, Long> {
  boolean existsById(Long id);
  
  ForgotUserModel findOneById(Long id);
  ForgotUserModel findOneByIdAndToken(Long id, String token);
}