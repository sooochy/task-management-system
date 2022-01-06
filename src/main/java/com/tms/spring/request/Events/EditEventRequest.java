package com.tms.spring.request.Events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class EditEventRequest {
    private Long id;
    private String name;
    private String description;
    private Long startDate;
    private Long endDate;
    private Boolean isMarked;
    private Long tstId;
    private String language;
    private String[] oldFiles;
    private MultipartFile[] files;
    private Short[] notifications;
    private String userEmail;
    private String userToken;
}