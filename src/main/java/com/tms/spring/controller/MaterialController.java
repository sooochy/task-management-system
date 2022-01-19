package com.tms.spring.controller;

import java.util.List;
import org.slf4j.Logger;
import java.util.Arrays;
import java.util.stream.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// Exceptions
import com.tms.spring.exception.UserNotExists;
import com.tms.spring.exception.NotValidException;
import com.tms.spring.exception.MaxUploadSizeExceededException;

// Models
import com.tms.spring.model.UserModel;
import com.tms.spring.model.TypeModel;
import com.tms.spring.model.FileModel;
import com.tms.spring.model.TeacherModel;
import com.tms.spring.model.SubjectModel;
import com.tms.spring.model.MaterialModel;
import com.tms.spring.model.TeacherSubjectTypeModel;

// Repositories
import com.tms.spring.repository.UserRepository;
import com.tms.spring.repository.FileRepository;
import com.tms.spring.repository.TypeRepository;
import com.tms.spring.repository.SubjectRepository;
import com.tms.spring.repository.MaterialRepository;
import com.tms.spring.repository.TeacherRepository;
import com.tms.spring.repository.TeacherSubjectTypeRepository;

// Requests
import com.tms.spring.request.SignIn.CheckLoginRequest;
import com.tms.spring.request.Materials.DeleteMaterialRequest;

// Responses
import com.tms.spring.response.DefaultMaterialStatus;

// Service
import com.tms.spring.file.FileStorageService;
import com.tms.spring.controller.FileController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/materials")
public class MaterialController {

  @Autowired
  UserRepository userRepository;

  @Autowired
  FileRepository fileRepository;

  @Autowired
  TypeRepository typeRepository;
  
  @Autowired
  SubjectRepository subjectRepository;
  
  @Autowired
  TeacherRepository teacherRepository;
  
  @Autowired
  FileStorageService fileStorageService;

  @Autowired
  MaterialRepository materialRepository;
  
  @Autowired
  TeacherSubjectTypeRepository teacherSubjectTypeRepository;

  /* ========================================================== [ ADD MATERIAL ] ======================================================= */

  @PostMapping("/add")
  public ResponseEntity<DefaultMaterialStatus> addMaterial(@RequestParam String name, @RequestParam String description, @RequestParam Integer day, @RequestParam Integer month, @RequestParam Integer year,
                                                           @RequestParam Long tstId, @RequestParam String userEmail, @RequestParam String userToken, @RequestParam(required = false) MultipartFile[] files) {
    
    // In request: name, description, date, tstId, [userEmail, userToken], file[]
    // In response: MaterialModel (with only files IDs)

    // TODO: more than 10 materials if user has TMS premium

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(userEmail);
    if(user == null || !user.checkUser(userToken)) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking materials limit and files (non premium user or expired subscription)
    if(user.getSubExpirationDate() == null || user.getSubExpirationDate().isBefore(LocalDateTime.now())) {
      if(files != null && user.getUploadedFiles() + files.length > 10) {
        throw new NotValidException("outOfFilesLimit");
      }

      long materialsAmount = materialRepository.countByUser(user);
      if(materialsAmount >= 10) {
        throw new NotValidException("outOfMaterialsLimit");
      }
    }

    // Checking if description and files are empty
    if((description == null || description.equals("")) && files == null) {
      throw new NotValidException("emptyDescriptionAndFiles");
    }

    // Input data validation
    if(name.length() < 1 || name.length() > 100) { throw new NotValidException("incorrectName"); }
    if(description == null) { description = ""; }
    if(!description.equals("")) { if(description.length() < 1 || description.length() > 2048) { throw new NotValidException("incorrectDescription"); } }

    // Creating new localDate
    if(year < 1900 || year > 2099) { throw new NotValidException("incorrectYear"); }
    if(month <= 0 || month > 12) { throw new NotValidException("incorrectMonth"); }
    if(day <= 0 || day > 31) { throw new NotValidException("incorrectDay"); }
    
    // Creating new localDate
    LocalDate localDate = LocalDate.of(year, month, day);

    // Looking for user's TST by id
    TeacherSubjectTypeModel teacherSubjectType = teacherSubjectTypeRepository.findOneById(tstId);
    if(teacherSubjectType == null) { throw new UserNotExists("TSTnotExists"); }

    // Checking if user has request's tstId assigned
    SubjectModel subject = subjectRepository.findOneByIdAndUser(teacherSubjectType.getSubject().getId(), user);
    TypeModel type = typeRepository.findOneByIdAndUser(teacherSubjectType.getType().getId(), user);
    
    if(user.getType() == 1) {
      TeacherModel teacher = teacherRepository.findOneByIdAndUser(teacherSubjectType.getTeacher().getId(), user);
      if(Stream.of(teacher, subject, type).anyMatch(value -> value.equals(null))) { throw new NotValidException("incorrectTST"); }
    } else {
      if(Stream.of(subject, type).anyMatch(value -> value.equals(null))) { throw new NotValidException("incorrectTST"); }
    }

    // Creating and saving new material
    MaterialModel material = new MaterialModel(name, description, localDate, teacherSubjectType);
    materialRepository.saveAndFlush(material);

    // Saving material's files to database with materialId
    try {
      if(files != null) { Arrays.asList(files).stream().map(file -> uploadFile(file, material.getId())).collect(Collectors.toList()); }
    } catch (Exception e) {
      throw new MaxUploadSizeExceededException("fileTooLarge");
    }

    // Finding all files attached to current material
    List<FileModel> materialFiles = fileRepository.findAllByMaterialId(material.getId());

    // Setting files to material
    material.setFiles(materialFiles);

    return new ResponseEntity<>(new DefaultMaterialStatus("materialAdded", material), HttpStatus.CREATED);
  }

  /* =========================================================== [ EDIT MATERIAL ] ======================================================= */

  @PostMapping("/edit")
  public ResponseEntity<DefaultMaterialStatus> editMaterial(@RequestParam Long id, @RequestParam String name, @RequestParam String description, @RequestParam Integer day, @RequestParam Integer month, 
                                                            @RequestParam Integer year, @RequestParam Long tstId, @RequestParam String userEmail, @RequestParam String userToken, 
                                                            @RequestParam(required = false) String[] oldFiles, @RequestParam(required = false) MultipartFile[] files) {
    
    // In request: id, name, description, date, tstId, [userEmail, userToken], oldFiles[], file[]
    // In response: if edited (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(userEmail);
    if(user == null || !user.checkUser(userToken)) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for material by id
    MaterialModel material = materialRepository.findOneByIdAndUser(id, user);
    if(material == null) { throw new UserNotExists("materialNotExists"); }

    // Input data validation
    if(name.length() < 1 || name.length() > 100) { throw new NotValidException("incorrectName"); }
    if(description == null) { description = ""; }
    if(!description.equals("")) { if(description.length() < 1 || description.length() > 2048) { throw new NotValidException("incorrectDescription"); } }

    // Creating new localDate
    if(year < 1900 || year > 2099) { throw new NotValidException("incorrectYear"); }
    if(month <= 0 || month > 12) { throw new NotValidException("incorrectMonth"); }
    if(day <= 0 || day > 31) { throw new NotValidException("incorrectDay"); }
    
    LocalDate localDate = LocalDate.of(year, month, day);
    
    // Looking for user's TST by id
    TeacherSubjectTypeModel teacherSubjectType = teacherSubjectTypeRepository.findOneById(tstId);
    if(teacherSubjectType == null) { throw new UserNotExists("TSTnotExists"); }

    // Checking if user has request's tstId assigned
    SubjectModel subject = subjectRepository.findOneByIdAndUser(teacherSubjectType.getSubject().getId(), user);
    TypeModel type = typeRepository.findOneByIdAndUser(teacherSubjectType.getType().getId(), user);

    if(user.getType() == 1) {
      TeacherModel teacher = teacherRepository.findOneByIdAndUser(teacherSubjectType.getTeacher().getId(), user);
      if(Stream.of(teacher, subject, type).anyMatch(value -> value.equals(null))) { throw new NotValidException("incorrectTST"); }
    } else {
      if(Stream.of(subject, type).anyMatch(value -> value.equals(null))) { throw new NotValidException("incorrectTST"); }
    }
    // Editing list of files in this material:
    // Searching the current list of material's files
    ArrayList<FileModel> listOfFiles = new ArrayList<>(material.getFiles());

    if(oldFiles != null && oldFiles.length != 0) {
      for(Integer i = 0; i < oldFiles.length; i++) {
        for(Integer j = 0; j < listOfFiles.size(); j++) {
          if(listOfFiles.get(j).getId().equals(oldFiles[i])) {
            listOfFiles.remove(listOfFiles.get(j));
            j--;
            break;
          }
        }
      }
    }
    fileRepository.deleteAll(listOfFiles);

    // Checking material's files limit after deleting old ones (non premium user or expired subscription)
    if(user.getSubExpirationDate() == null || user.getSubExpirationDate().isBefore(LocalDateTime.now())) {
      if(files != null && user.getUploadedFiles() + files.length > 10) {
        throw new NotValidException("outOfFilesLimit");
      }
    }

    // Saving new material's files to database
    try {
      if(files != null) { Arrays.asList(files).stream().map(file -> uploadFile(file, material.getId())).collect(Collectors.toList()); }
    } catch (Exception e) {
      throw new MaxUploadSizeExceededException("fileTooLarge");
    }

    // Setting name, description, date and TSS
    MaterialModel editedMaterial = new MaterialModel(id, name, description, localDate, teacherSubjectType);
    
    // Assigning files to material
    List<FileModel> materialFiles = fileRepository.findAllByMaterialId(id);
    editedMaterial.setFiles(materialFiles);

    // Saving material with edited data
    materialRepository.saveAndFlush(editedMaterial);

    return new ResponseEntity<>(new DefaultMaterialStatus("materialEdited", editedMaterial), HttpStatus.CREATED);
  }

  /* ========================================================== [ DELETE MATERIAL ] ====================================================== */

  @PostMapping("/delete")
  public ResponseEntity<DefaultMaterialStatus> deleteMaterial(@RequestBody DeleteMaterialRequest request) {
    // In request: id, [userEmail, userToken]
    // In response: if deleted (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    MaterialModel material = materialRepository.findOneByIdAndUser(request.getId(), user);
    if(material == null) {
      throw new UserNotExists("materialNotExists");
    }

    // Deleting material
    materialRepository.delete(material);

    return new ResponseEntity<>(new DefaultMaterialStatus("materialDeleted"), HttpStatus.ACCEPTED);
  }

  /* =========================================================== [ GET MATERIAL ] ======================================================= */

  @PostMapping("/get")
  public ResponseEntity<List<MaterialModel>> getMaterials(@RequestBody CheckLoginRequest request) {
    // In request: type, [userEmail, userToken]
    // In response: list of materials by userId

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if accout type is valid
    if(user.getType() != request.getType()) {
      throw new UserNotExists("typeError");
    }

    return new ResponseEntity<>(materialRepository.findAllByUser(user), HttpStatus.OK);
  }

  /* ============================================================ [ UPLOAD FILE ] ======================================================= */

  public FileModel uploadFile(MultipartFile file, Long materialId) {
    FileModel fileModel = fileStorageService.storeMaterialFile(file, materialId);
    
    return fileModel;
  }
}