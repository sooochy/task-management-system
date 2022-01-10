package com.tms.spring.repository;

import com.tms.spring.model.UserModel;
import com.tms.spring.model.UniversityModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<UniversityModel, Long> {
    UniversityModel findOneByIdAndUser(Long id, UserModel user);
    UniversityModel findOneByNameAndUser(String name, UserModel user);
}