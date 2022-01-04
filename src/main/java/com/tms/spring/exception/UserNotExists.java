package com.tms.spring.exception;

public class UserNotExists extends RuntimeException {
    
    public UserNotExists(String msg) {
        super(msg);
    }
}