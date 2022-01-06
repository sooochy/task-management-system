package com.tms.spring.request.Marks;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteMarkRequest {
    private Long id;
    private String userEmail;
    private String userToken;
}