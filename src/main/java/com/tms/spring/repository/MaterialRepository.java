package com.tms.spring.repository;

import java.util.List;
import com.tms.spring.model.UserModel;
import com.tms.spring.model.MaterialModel;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialModel, String> {

    @Query(value = "SELECT COUNT(*) FROM material INNER JOIN teacher_subject_type ON material.tst_id = teacher_subject_type.id INNER JOIN subject ON teacher_subject_type.subject_id = subject.id WHERE subject.user_id = ?1", nativeQuery = true)
    long countByUser(UserModel user);

    MaterialModel findOneById(Long id);

    @Query(value = "SELECT material.* FROM material INNER JOIN teacher_subject_type ON material.tst_id = teacher_subject_type.id INNER JOIN subject ON teacher_subject_type.subject_id = subject.id WHERE material.id = ?1 AND subject.user_id = ?2", nativeQuery = true)
    MaterialModel findOneByIdAndUser(Long id, UserModel user);

    @Query(value = "SELECT material.* FROM material INNER JOIN teacher_subject_type ON material.tst_id = teacher_subject_type.id INNER JOIN subject ON teacher_subject_type.subject_id = subject.id WHERE subject.user_id = ?1", nativeQuery = true)
    List<MaterialModel> findAllByUser(UserModel user);
}