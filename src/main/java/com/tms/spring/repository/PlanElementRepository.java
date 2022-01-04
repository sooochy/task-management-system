package com.tms.spring.repository;

import java.util.List;
import com.tms.spring.model.UserModel;
import com.tms.spring.model.PlanElementModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanElementRepository extends JpaRepository<PlanElementModel, Long> {
    PlanElementModel findOneByIdAndUser(Long id, UserModel user);
}