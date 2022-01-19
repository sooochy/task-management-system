package com.tms.spring.repository;

import com.tms.spring.model.UserModel;
import com.tms.spring.model.FacultyModel;
import com.tms.spring.model.UniversityModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacultyRepository extends JpaRepository<FacultyModel, Long> {
    @Query(value = "SELECT faculty.* FROM faculty INNER JOIN university ON faculty.university_id = university.id WHERE faculty.id = ?1 AND university.user_id = ?2", nativeQuery = true)
    FacultyModel findOneByIdAndUser(Long id, UserModel user);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 'true' ELSE 'false' END FROM faculty INNER JOIN university ON faculty.university_id = university.id WHERE faculty.name = ?1 AND faculty.university_id = ?2 AND university.user_id = ?3", nativeQuery = true)
    Boolean existsByNameAndUniversityAndUser(String name, UniversityModel university, UserModel user);
}