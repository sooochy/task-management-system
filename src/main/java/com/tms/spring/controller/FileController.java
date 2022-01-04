package com.tms.spring.controller;

import java.util.stream.*;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

// Exceptions
import com.tms.spring.exception.UserNotExists;
import com.tms.spring.exception.NotValidException;
import com.tms.spring.exception.MaxUploadSizeExceededException;

// Models
import com.tms.spring.model.FileModel;

// Service
import com.tms.spring.file.FileStorageService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/files")
public class FileController {

  @Autowired
  FileStorageService fileStorageService;

  /* =========================================================== [ GET FILE BY ID ] ======================================================= */

  @GetMapping("/download/{id}")
  public ResponseEntity<Resource> downloadFile(@PathVariable String id) {
      // Load file from database
      FileModel fileModel = fileStorageService.getFile(id);

      return ResponseEntity.ok()
              .contentType(MediaType.parseMediaType(fileModel.getType()))
              .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileModel.getName() + "\"")
              .body(new ByteArrayResource(fileModel.getData()));
  }
}