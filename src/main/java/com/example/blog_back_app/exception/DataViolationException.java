package com.example.blog_back_app.exception;

public class DataViolationException extends RuntimeException {
    public DataViolationException(String message) {
        super(message);
    }
}
