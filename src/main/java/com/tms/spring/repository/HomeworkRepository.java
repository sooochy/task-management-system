package com.tms.spring.repository;

import java.util.List;
import com.tms.spring.model.UserModel;
import com.tms.spring.model.HomeworkModel;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface HomeworkRepository extends JpaRepository<HomeworkModel, String> {
    HomeworkModel findOneById(Long id);
    HomeworkModel findOneByIdAndUser(Long id, UserModel user);
    long countByIsDoneAndUser(Boolean isDone, UserModel user);
}