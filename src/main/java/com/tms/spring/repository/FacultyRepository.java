package com.tms.spring.repository;

import com.tms.spring.model.UserModel;
import com.tms.spring.model.FacultyModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacultyRepository extends JpaRepository<FacultyModel, Long> {
    Boolean existsByNameAndUser(String name, UserModel user);
    FacultyModel findOneByIdAndUser(Long id, UserModel user);
}