package com.tms.spring.request.Homeworks;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class EditHomeworkRequest {
    private Long id;
    private String name;
    private String description;
    private Long deadline;
    private Long estimatedTime;
    private Long date;
    private Boolean isMarked;
    private Long tstId;
    private String language;
    private String[] oldFiles;
    private MultipartFile[] files;
    private Short[] notifications;
    private String userEmail;
    private String userToken;
}