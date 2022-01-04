package com.tms.spring.controller;

import java.util.List;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

// Exceptions
import com.tms.spring.exception.UserExists;
import com.tms.spring.exception.UserNotExists;
import com.tms.spring.exception.NotValidException;

// Models
import com.tms.spring.model.UserModel;
import com.tms.spring.model.TypeModel;
import com.tms.spring.model.TeacherModel;
import com.tms.spring.model.SubjectModel;
import com.tms.spring.model.TeacherSubjectTypeModel;

// Repositories
import com.tms.spring.repository.UserRepository;
import com.tms.spring.repository.TypeRepository;
import com.tms.spring.repository.TeacherRepository;
import com.tms.spring.repository.SubjectRepository;
import com.tms.spring.repository.TeacherSubjectTypeRepository;

// Requests
import com.tms.spring.request.Subjects.TeacherType;
import com.tms.spring.request.Subjects.AddTypeRequest;
import com.tms.spring.request.Subjects.EditTypeRequest;
import com.tms.spring.request.SignIn.CheckLoginRequest;
import com.tms.spring.request.Subjects.DeleteTypeRequest;
import com.tms.spring.request.Subjects.AddUserSubjectRequest;
import com.tms.spring.request.Subjects.EditUserSubjectRequest;
import com.tms.spring.request.Subjects.DeleteUserSubjectRequest;

// Responses
import com.tms.spring.response.DefaultSubjectStatus;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/subjects")
public class SubjectController {

  @Autowired
  TypeRepository typeRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  SubjectRepository subjectRepository; 
  
  @Autowired
  TeacherRepository teacherRepository;
  
  @Autowired
  TeacherSubjectTypeRepository teacherSubjectTypeRepository;

  /* ******************************************************************************************************************************* */
  /*                                                             [ TYPES ]                                                           */
  /* ******************************************************************************************************************************* */

  /* ========================================================== [ ADD TYPE ] ======================================================= */

  @PostMapping("/types/add")
  public ResponseEntity<DefaultSubjectStatus> addType(@RequestBody AddTypeRequest request) {
    // In request: name, [userEmail, userToken]
    // In response: if added (OK), id

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Input data validation
    if(request.getName().length() > 50 && request.getName().length() < 2) { throw new NotValidException("nameNotValid"); }

    // Check if type already exists
    Boolean ifExists = typeRepository.existsByNameAndUser(request.getName(), user);
    if(ifExists) {
        throw new UserExists("typeExists");
    }

    // Saving new model to database
    TypeModel type = new TypeModel(request.getName(), user);
    typeRepository.save(type);

    return new ResponseEntity<>(new DefaultSubjectStatus("typeAdded", type.getId()), HttpStatus.CREATED);
  }

  /* ========================================================== [ EDIT TYPE ] ======================================================= */

  @PostMapping("/types/edit")
  public ResponseEntity<DefaultSubjectStatus> editType(@RequestBody EditTypeRequest request) {
    // In request: id, name, [userEmail, userToken]
    // In response: if edited (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for user's type to edit by id 
    TypeModel existingType = typeRepository.findOneByIdAndUser(request.getId(), user);
    if(existingType == null) {
      throw new UserNotExists("typeNotExists");
    }

    // Input data validation
    if(request.getName().length() > 50 && request.getName().length() < 2) { throw new NotValidException("nameNotValid"); }
    
    // Chcecking if type that user want to set as new already exists 
    Boolean ifExists = typeRepository.existsByNameAndUser(request.getName(), user);
    if(ifExists) {
        throw new UserExists("typeExists");
    }

    // Saving teacher to 'teachers' table with updated data
    existingType.setName(request.getName());
    typeRepository.save(existingType);

    return new ResponseEntity<>(new DefaultSubjectStatus("typeEdited"), HttpStatus.ACCEPTED);
  }

  /* ========================================================== [ DELETE TYPE ] ====================================================== */

  @PostMapping("/types/delete")
  public ResponseEntity<DefaultSubjectStatus> deleteType(@RequestBody DeleteTypeRequest request) {
    // In request: id, [userEmail, userToken]
    // In response: if deleted (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for user's type to delete by id 
    TypeModel type = typeRepository.findOneByIdAndUser(request.getId(), user);
    if(type == null) {
      throw new UserNotExists("typeNotExists");
    }
    
    // Deleting type from database
    typeRepository.delete(type);

    return new ResponseEntity<>(new DefaultSubjectStatus("typeDeleted"), HttpStatus.ACCEPTED);
  }

  /* =========================================================== [ GET TYPES ] ======================================================= */

  @PostMapping("/types/get")
  public ResponseEntity<List<TypeModel>> getTypes(@RequestBody CheckLoginRequest request) {
    // In request: [userEmail, userToken]
    // In response: list of types by userId

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    return new ResponseEntity<>(user.getTypes(), HttpStatus.OK);
  }

  // ******************************************************************************************************************************* //
  //                                                             [ SUBJECTS ]                                                        //
  // ******************************************************************************************************************************* //

  /* ========================================================= [ ADD SUBJECT ] ===================================================== */

  /* Request example:
  
      "name": "KTP",
      "teacherType": [
          {
              "typeId": 1,
              "teacherId": 3
          },
          {
              "typeId": 2,
              "teacherId": 4
          },
          {
              "typeId": 6,
              "teacherId": 5
          }
      ],
      
  */

  @PostMapping("/user/add")
  public ResponseEntity<DefaultSubjectStatus> addSubject(@RequestBody AddUserSubjectRequest request) {
    // In request: name, teacherType[ {typeId, teacherId}, ...], [userEmail, userToken]
    // In response: if added (OK), id

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Checking if user has assigned type of subject and teacher
    Long typeId, teacherId;
    TypeModel existingType;
    TeacherModel existingTeacher;
    Boolean ifTypeExists, ifTeacherExists;

    for(Integer i = 0; i < request.getTeacherType().size(); i++) {
      ifTypeExists = typeRepository.existsByIdAndUser(request.getTeacherType().get(i).getTypeId(), user);
      ifTeacherExists = teacherRepository.existsByIdAndUser(request.getTeacherType().get(i).getTeacherId(), user);

      if(!ifTypeExists) { throw new UserNotExists("typeNotExists"); }
      if(!ifTeacherExists) { throw new UserNotExists("teacherNotExists"); }

      for(Integer j = 0; j < request.getTeacherType().size(); j++) 
        if(!i.equals(j)) 
          if(request.getTeacherType().get(i).equalsTeacherType(request.getTeacherType().get(j)))
            throw new NotValidException("duplicated");
    }

    // Check if subject already exists
    Boolean ifSubjectExists = subjectRepository.existsByNameAndUser(request.getName(), user);
    if(ifSubjectExists) {
      throw new UserExists("subjectExists");
    }

    // Input data validation
    if(request.getName().length() > 100 && request.getName().length() < 2) { throw new NotValidException("nameNotValid"); }

    // Limiting groups to 5 
    if(request.getTeacherType().size() > 5) {
      throw new NotValidException("invalidGroupsNumber");
    }

    // Saving new subject to database
    SubjectModel subject = new SubjectModel(request.getName(), user);
    subjectRepository.save(subject);

    // Saving typeId, subjectId, teacherId to 'teacher_subject_type'
    TypeModel type;
    TeacherModel teacher;
    TeacherSubjectTypeModel teacherSubjectType = new TeacherSubjectTypeModel();

    for (Integer i = 0; i < request.getTeacherType().size(); i++) {
      teacher = teacherRepository.findOneById(request.getTeacherType().get(i).getTeacherId());
      type = typeRepository.findOneById(request.getTeacherType().get(i).getTypeId());
      
      teacherSubjectType = new TeacherSubjectTypeModel(teacher, subject, type);
      teacherSubjectTypeRepository.save(teacherSubjectType);
    }

    return new ResponseEntity<>(new DefaultSubjectStatus("subjectAdded", subject.getId()), HttpStatus.CREATED);
  }

  /* ========================================================= [ EDIT SUBJECT ] ====================================================== */

  /* Request example:

    "id": "1"
    "name": "KTP",
    "teacherType": [
        {
            "id": 13
            "typeId": 1,
            "teacherId": 3
        },
        {
            "id": 14
            "typeId": 2,
            "teacherId": 4
        },
        {
            "id": 133
            "typeId": 6,
            "teacherId": 5
        }
    ],

  */

  @PostMapping("/user/edit")
  public ResponseEntity<DefaultSubjectStatus> editSubject(@RequestBody EditUserSubjectRequest request) {
    // In request: id, name, teacherType[ {id, typeId, teacherId}, ...], [userEmail, userToken]
    // In response: if edited (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Limiting groups to 5 
    if(request.getTeacherType().size() > 5) {
      throw new NotValidException("invalidGroupsNumber");
    }

    // Checking if user has assigned type of subject and teacher wanted to edit
    Boolean ifTypeExists, ifTeacherExists, ifSubjectExists;
    
    for(Integer i = 0; i < request.getTeacherType().size(); i++) {
      ifTypeExists = typeRepository.existsByIdAndUser(request.getTeacherType().get(i).getTypeId(), user);
      ifTeacherExists = teacherRepository.existsByIdAndUser(request.getTeacherType().get(i).getTeacherId(), user);
      
      if(!ifTypeExists) { throw new UserNotExists("typeNotExists"); }
      if(!ifTeacherExists) { throw new UserNotExists("teacherNotExists"); }

      for(Integer j = 0; j < request.getTeacherType().size(); j++) 
        if(!i.equals(j)) 
          if(request.getTeacherType().get(i).equalsTeacherType(request.getTeacherType().get(j)))
            throw new NotValidException("duplicated");
    }
    
    // Input data validation
    if(request.getName().length() > 100 && request.getName().length() < 2) { throw new NotValidException("nameNotValid"); }

    // Checking if user has this subject to edit
    SubjectModel existingSubject = subjectRepository.findOneByIdAndUser(request.getId(), user);
    if(existingSubject == null) {
      throw new UserNotExists("subjectNotExists");
    }

    // Chcecking if name of subject user want to edit already exists in database
    if(subjectRepository.existsByNameAndUserAndIdNot(request.getName(), user, request.getId())) {
      throw new UserExists("subjectExists");
    }

    // Saving subject to 'subject' table with updated data
    existingSubject.setName(request.getName());
    subjectRepository.save(existingSubject);

    // Saving data with updated (edited) values
    TypeModel type;
    TeacherModel teacher;
    TeacherType teacherType;
    TeacherSubjectTypeModel teacherSubjectType;
    List<TeacherSubjectTypeModel> toDeleteTST = new ArrayList<>(existingSubject.getTeacherSubjectTypes());

    for(Integer i = 0; i < request.getTeacherType().size(); i++) {
      teacherType = request.getTeacherType().get(i);

      if(teacherType.getId() != null) {
        for(Integer j = 0; j < existingSubject.getTeacherSubjectTypes().size(); j++) {
          teacherSubjectType = existingSubject.getTeacherSubjectTypes().get(j);
          
          if(teacherType.getId().equals(teacherSubjectType.getId())) {
            teacher = teacherRepository.findOneById(teacherType.getTeacherId());
            type = typeRepository.findOneById(teacherType.getTypeId());

            toDeleteTST.remove(teacherSubjectType);

            teacherSubjectType.setTeacher(teacher);
            teacherSubjectType.setType(type);
            teacherSubjectTypeRepository.save(teacherSubjectType);
          }
        }
      } else {
        teacher = teacherRepository.findOneById(teacherType.getTeacherId());
        type = typeRepository.findOneById(teacherType.getTypeId());

        teacherSubjectType = new TeacherSubjectTypeModel(teacher, existingSubject, type);
        teacherSubjectTypeRepository.save(teacherSubjectType);
      }
    }

    teacherSubjectTypeRepository.deleteAll(toDeleteTST);

    return new ResponseEntity<>(new DefaultSubjectStatus("subjectEdited"), HttpStatus.ACCEPTED);
  }

  /* ======================================================== [ DELETE SUBJECT ] ===================================================== */

  @PostMapping("/user/delete")
  public ResponseEntity<DefaultSubjectStatus> deleteSubject(@RequestBody DeleteUserSubjectRequest request) {
    // In request: id, [userEmail, userToken]
    // In response: if deleted (OK)

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }

    // Looking for user's type to delete by id 
    SubjectModel subject = subjectRepository.findOneByIdAndUser(request.getId(), user);
    if(subject == null) {
      throw new UserNotExists("subjectNotExists");
    }
    
    // Deleting subject from database
    subjectRepository.delete(subject);

    return new ResponseEntity<>(new DefaultSubjectStatus("subjectDeleted"), HttpStatus.ACCEPTED);
  }

  /* ========================================================== [ GET SUBJECT ] ====================================================== */

  @PostMapping("/user/get")
  public ResponseEntity<List<SubjectModel>> getSubjects(@RequestBody CheckLoginRequest request) {
    // In request: [userEmail, userToken]
    // In response: list of subjects by userId

    // Need to get user's already hashed password through email in 'user' table and check if email exists in 'user' table
    UserModel user = userRepository.findOneByEmail(request.getUserEmail());
    if(user == null || !user.checkUser(request.getUserToken())) {
      throw new UserNotExists("tokenNotValid");
    }
    
    return new ResponseEntity<>(user.getSubjects(), HttpStatus.OK);
  }
}