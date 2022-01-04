package com.tms.spring.exception;

public class NotValidException extends RuntimeException {
    
    public NotValidException(String msg) {
        super(msg);
    }
}