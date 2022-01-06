package com.tms.spring.request.Marks;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddMarkRequest {
    private Float mark;
    private Long date;
    private String description;
    private Long eventId;
    private Long homeworkId;
    private Long tstId;
    private String userEmail;
    private String userToken;
}