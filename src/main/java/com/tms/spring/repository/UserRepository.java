package com.tms.spring.repository;

import com.tms.spring.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, Long> {
  boolean existsByEmail(String email);
  
  UserModel findOneById(Long id);
  UserModel findOneByEmail(String email);
  UserModel findOneByEmailAndPassword(String email, String password);
}