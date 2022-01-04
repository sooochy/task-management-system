package com.tms.spring.repository;

import java.util.List;
import com.tms.spring.model.NotificationModel;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationModel, String> {
    List<NotificationModel> findAll();
    List<NotificationModel> findAllByHomeworkId(Long id);
}