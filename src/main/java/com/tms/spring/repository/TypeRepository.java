package com.tms.spring.repository;

import com.tms.spring.model.UserModel;
import com.tms.spring.model.TypeModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeRepository extends JpaRepository<TypeModel, Long> {
    TypeModel findOneById(Long id);
    TypeModel findOneByIdAndUser(Long id, UserModel user);
    
    Boolean existsByIdAndUser(Long id, UserModel user);
    Boolean existsByNameAndUser(String name, UserModel user);
}