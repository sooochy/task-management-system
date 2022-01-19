package com.tms.spring.repository;

import java.util.List;
import com.tms.spring.model.MarkModel;
import com.tms.spring.model.UserModel;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface MarkRepository extends JpaRepository<MarkModel, String> {
    @Query(value = "SELECT mark.* FROM mark INNER JOIN teacher_subject_type ON mark.tst_id = teacher_subject_type.id INNER JOIN subject ON teacher_subject_type.subject_id = subject.id WHERE subject.user_id = ?1", nativeQuery = true)
    List<MarkModel> findAllByUser(UserModel user);
    
    @Query(value = "SELECT mark.* FROM mark INNER JOIN teacher_subject_type ON mark.tst_id = teacher_subject_type.id INNER JOIN subject ON teacher_subject_type.subject_id = subject.id WHERE mark.id = ?1 AND subject.user_id = ?2", nativeQuery = true)
    MarkModel findOneByIdAndUser(Long id, UserModel user);
    
    @Query(value = "SELECT mark.* FROM mark INNER JOIN teacher_subject_type ON mark.tst_id = teacher_subject_type.id INNER JOIN subject ON teacher_subject_type.subject_id = subject.id WHERE mark.event_id = ?1 AND subject.user_id = ?2", nativeQuery = true)
    MarkModel findOneByEventIdAndUser(Long id, UserModel user);
    
    @Query(value = "SELECT mark.* FROM mark INNER JOIN teacher_subject_type ON mark.tst_id = teacher_subject_type.id INNER JOIN subject ON teacher_subject_type.subject_id = subject.id WHERE mark.homework_id = ?1 AND subject.user_id = ?2", nativeQuery = true)
    MarkModel findOneByHomeworkIdAndUser(Long id, UserModel user);
}