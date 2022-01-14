package com.tms.spring.repository;

import java.util.List;
import com.tms.spring.model.UserModel;
import com.tms.spring.model.MaterialModel;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialModel, String> {
    long countByUser(UserModel user);
    MaterialModel findOneById(Long id);
    MaterialModel findOneByIdAndUser(Long id, UserModel user);
    List<MaterialModel> findAllByUser(UserModel user);
}