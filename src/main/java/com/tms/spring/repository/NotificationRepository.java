package com.tms.spring.repository;

import java.util.List;
import com.tms.spring.model.UserModel;
import com.tms.spring.model.NotificationModel;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationModel, String> {

    @Query(value = "SELECT * FROM notification WHERE is_sent = ?1 AND alert_date <= NOW()", nativeQuery = true)
    List<NotificationModel> findAllByIsSentAndBeforeNow(Boolean isSent);

    List<NotificationModel> findAllByEventId(Long id);

    List<NotificationModel> findAllByHomeworkId(Long id);

    @Query(value = "SELECT n.* FROM notification n INNER JOIN teacher_subject_type tst ON tst.id = IF(n.homework_id IS NOT NULL, (SELECT tst_id FROM homework WHERE homework.id = n.homework_id), (SELECT tst_id FROM event WHERE event.id = n.event_id)) INNER JOIN subject ON tst.subject_id = subject.id WHERE n.id = ?1 AND subject.user_id = ?2", nativeQuery = true)
    NotificationModel findOneByIdAndUser(Long id, UserModel user);

    @Query(value = "SELECT n.* FROM notification n INNER JOIN teacher_subject_type tst ON tst.id = IF(n.homework_id IS NOT NULL, (SELECT tst_id FROM homework WHERE homework.id = n.homework_id), (SELECT tst_id FROM event WHERE event.id = n.event_id)) INNER JOIN subject ON tst.subject_id = subject.id WHERE subject.user_id = ?1 ORDER BY is_viewed ASC, alert_date DESC", nativeQuery = true)
    List<NotificationModel> findAllByUserOrderByIsViewedAscAlertDateDesc(UserModel user);
}