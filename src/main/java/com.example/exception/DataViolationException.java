package com.example.exception;

public class DataViolationException extends RuntimeException {
    public DataViolationException(String message) {
        super(message);
    }
}
