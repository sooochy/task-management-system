package com.tms.spring.request.Homeworks;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteHomeworkRequest {
    private Long id;
    private String userEmail;
    private String userToken;
}