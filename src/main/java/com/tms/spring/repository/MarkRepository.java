package com.tms.spring.repository;

import java.util.List;
import com.tms.spring.model.MarkModel;
import com.tms.spring.model.UserModel;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface MarkRepository extends JpaRepository<MarkModel, String> {
    Boolean existsByEventIdAndUser(Long id, UserModel user);
    Boolean existsByHomeworkIdAndUser(Long id, UserModel user);

    MarkModel findOneByIdAndUser(Long id, UserModel user);
    MarkModel findOneByEventIdAndUser(Long id, UserModel user);
    MarkModel findOneByHomeworkIdAndUser(Long id, UserModel user);
}