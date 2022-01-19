package com.tms.spring.repository;

import java.util.List;
import com.tms.spring.model.UserModel;
import com.tms.spring.model.FieldModel;
import com.tms.spring.model.FacultyModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldRepository extends JpaRepository<FieldModel, Long> {
    @Query(value = "SELECT field.* FROM field INNER JOIN faculty ON field.faculty_id = faculty.id INNER JOIN university ON faculty.university_id = university.id WHERE university.user_id = ?1", nativeQuery = true)
    List<FieldModel> findAllByUser(UserModel user);

    @Query(value = "SELECT field.* FROM field INNER JOIN faculty ON field.faculty_id = faculty.id INNER JOIN university ON faculty.university_id = university.id WHERE field.id = ?1 AND university.user_id = ?2", nativeQuery = true)
    FieldModel findOneByIdAndUser(Long id, UserModel user);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 'true' ELSE 'false' END FROM field INNER JOIN faculty ON field.faculty_id = faculty.id INNER JOIN university ON faculty.university_id = university.id WHERE field.name = ?1 AND field.faculty_id = ?2 AND university.user_id = ?3", nativeQuery = true)
    Boolean existsByNameAndFacultyAndUser(String name, FacultyModel faculty, UserModel user);
}