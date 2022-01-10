package com.tms.spring.request.Marks;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditMarkRequest {
    private Long id;
    private Float mark;
    private Long date;
    private String description;
    private String userEmail;
    private String userToken;
}