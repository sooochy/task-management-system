package com.tms.spring.repository;

import java.util.List;
import com.tms.spring.model.TeacherSubjectTypeModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherSubjectTypeRepository extends JpaRepository<TeacherSubjectTypeModel, Long> {
    TeacherSubjectTypeModel findOneById(Long id);
    List<TeacherSubjectTypeModel> findAllBySubjectId(Long id);
}