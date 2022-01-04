package com.tms.spring.request.Homeworks;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinishHomeworkRequest {
    private Long id;
    private Boolean isDone;
    private String userEmail;
    private String userToken;
}