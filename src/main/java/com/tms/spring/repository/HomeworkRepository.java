package com.tms.spring.repository;

import java.util.List;
import com.tms.spring.model.UserModel;
import com.tms.spring.model.HomeworkModel;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface HomeworkRepository extends JpaRepository<HomeworkModel, String> {
    HomeworkModel findOneById(Long id);

    @Query(value = "SELECT homework.* FROM homework INNER JOIN teacher_subject_type ON homework.tst_id = teacher_subject_type.id INNER JOIN subject ON teacher_subject_type.subject_id = subject.id WHERE subject.user_id = ?1", nativeQuery = true)
    List<HomeworkModel> findAllByUser(UserModel user);

    @Query(value = "SELECT homework.* FROM homework INNER JOIN teacher_subject_type ON homework.tst_id = teacher_subject_type.id INNER JOIN subject ON teacher_subject_type.subject_id = subject.id WHERE homework.id = ?1 AND subject.user_id = ?2", nativeQuery = true)
    HomeworkModel findOneByIdAndUser(Long id, UserModel user);

    @Query(value = "SELECT COUNT(*) FROM homework INNER JOIN teacher_subject_type ON homework.tst_id = teacher_subject_type.id INNER JOIN subject ON teacher_subject_type.subject_id = subject.id WHERE homework.is_done = ?1 AND subject.user_id = ?2", nativeQuery = true)
    long countByIsDoneAndUser(Boolean isDone, UserModel user);

    @Query(value = "SELECT subject.user_id FROM homework INNER JOIN teacher_subject_type ON homework.tst_id = teacher_subject_type.id INNER JOIN subject ON teacher_subject_type.subject_id = subject.id WHERE homework.id = ?1", nativeQuery = true)
    Long getUserId(Long id);
}