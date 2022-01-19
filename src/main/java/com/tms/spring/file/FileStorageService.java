package com.tms.spring.file;

import java.io.IOException;
import com.tms.spring.model.FileModel;
import com.tms.spring.model.EventModel;
import com.tms.spring.model.HomeworkModel;
import com.tms.spring.model.MaterialModel;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import com.tms.spring.repository.FileRepository;
import com.tms.spring.repository.EventRepository;
import com.tms.spring.repository.EventRepository;
import com.tms.spring.repository.HomeworkRepository;
import com.tms.spring.repository.MaterialRepository;
import com.tms.spring.exception.FileStorageException;
import com.tms.spring.exception.FileNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class FileStorageService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private HomeworkRepository homeworkRepository;

    public FileModel storeMaterialFile(MultipartFile file, Long materialId) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("invalidPathSequence");
            }

            MaterialModel material = materialRepository.findOneById(materialId);
            FileModel fileModel = new FileModel(fileName, file.getContentType(), file.getBytes(), material, file.getSize());

            return fileRepository.saveAndFlush(fileModel);
        } catch (IOException ex) {
            throw new FileStorageException("couldNotStoreFile");
        }
    }

    public FileModel storeHomeworkFile(MultipartFile file, Long homeworkId) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("invalidPathSequence");
            }
            
            HomeworkModel homework = homeworkRepository.findOneById(homeworkId);
            FileModel fileModel = new FileModel(fileName, file.getContentType(), file.getBytes(), homework, file.getSize());

            return fileRepository.saveAndFlush(fileModel);
        } catch (IOException ex) {
            throw new FileStorageException("couldNotStoreFile");
        }
    }

    public FileModel storeEventFile(MultipartFile file, Long eventId) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("invalidPathSequence");
            }

            EventModel event = eventRepository.findOneById(eventId);
            FileModel fileModel = new FileModel(fileName, file.getContentType(), file.getBytes(), event, file.getSize());

            return fileRepository.saveAndFlush(fileModel);
        } catch (IOException ex) {
            throw new FileStorageException("couldNotStoreFile");
        }
    }

    public FileModel getFile(String id) {
        return fileRepository.findById(id).orElseThrow(() -> new FileNotFoundException("notFound"));
    }
}