package com.tms.spring.exception;

public class MaxUploadSizeExceededException extends RuntimeException {
    public MaxUploadSizeExceededException(String msg) {
        super(msg);
    }

    public MaxUploadSizeExceededException(String msg, Throwable cause) {
        super(msg, cause);
    }
}