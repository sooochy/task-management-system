package com.tms.spring.exception;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String msg) {
        super(msg);
    }
}