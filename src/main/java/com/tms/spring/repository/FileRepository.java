package com.tms.spring.repository;

import java.util.List;
import com.tms.spring.model.FileModel;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface FileRepository extends JpaRepository<FileModel, String> {
    FileModel findOneById(String id);
    List<FileModel> findAllByMaterialId(Long id);
    List<FileModel> findAllByHomeworkId(Long id);
}