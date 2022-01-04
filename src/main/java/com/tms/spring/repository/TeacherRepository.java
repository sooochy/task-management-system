package com.tms.spring.repository;

import java.util.List;
import com.tms.spring.model.UserModel;
import com.tms.spring.model.TeacherModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<TeacherModel, Long> {
    boolean existsById(Long id); 
    Boolean existsByIdAndUser(Long id, UserModel user);
    Boolean existsByFirstNameAndLastName(String firstName, String lastName);

    TeacherModel findOneById(Long id);
    List<TeacherModel> findAllByUser(UserModel user);
    TeacherModel findOneByIdAndUser(Long id, UserModel user);
    
}