package com.tms.spring.repository;

import com.tms.spring.model.UserModel;
import com.tms.spring.model.EventModel;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface EventRepository extends JpaRepository<EventModel, String> {
    EventModel findOneById(Long id);

    EventModel findOneByIdAndUser(Long id, UserModel user);
}