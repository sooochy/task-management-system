package com.tms.spring.repository;

import com.tms.spring.model.UserModel;
import com.tms.spring.model.FieldModel;
import com.tms.spring.model.FacultyModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldRepository extends JpaRepository<FieldModel, Long> {
    FieldModel findOneByIdAndUser(Long id, UserModel user);
    Boolean existsByNameAndFacultyAndUser(String name, FacultyModel faculty, UserModel user);
}