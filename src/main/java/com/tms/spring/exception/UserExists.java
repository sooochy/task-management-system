package com.tms.spring.exception;

public class UserExists extends RuntimeException {
    
    public UserExists(String msg) {
        super(msg);
    }
}