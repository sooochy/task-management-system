package com.tms.spring.request.Events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteEventRequest {
    private Long id;
    private String userEmail;
    private String userToken;
}