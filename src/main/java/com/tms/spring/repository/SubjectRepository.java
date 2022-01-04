package com.tms.spring.repository;

import com.tms.spring.model.UserModel;
import com.tms.spring.model.SubjectModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<SubjectModel, Long> {
    Boolean existsByNameAndUser(String name, UserModel user);
    Boolean existsByNameAndUserAndIdNot(String name, UserModel user, Long id);

    SubjectModel findOneByIdAndUser(Long id, UserModel user);
}