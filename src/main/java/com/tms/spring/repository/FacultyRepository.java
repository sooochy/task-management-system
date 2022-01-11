package com.tms.spring.repository;

import com.tms.spring.model.UserModel;
import com.tms.spring.model.FacultyModel;
import com.tms.spring.model.UniversityModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacultyRepository extends JpaRepository<FacultyModel, Long> {
    FacultyModel findOneByIdAndUser(Long id, UserModel user);
    Boolean existsByNameAndUniversityAndUser(String name, UniversityModel university, UserModel user);
}