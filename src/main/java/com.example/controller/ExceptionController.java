package com.example.controller;

import com.example.dto.ErrorResponseDto;
import com.example.exception.DataViolationException;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.*;

@Component
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler({NotFoundException.class, EmptyResultDataAccessException.class})
    @ResponseStatus(NOT_FOUND)
    public ErrorResponseDto handleNotFoundException(final Exception exception) {
        return new ErrorResponseDto(exception.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler({DataViolationException.class})
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponseDto handleDataViolationException(final Exception exception) {
        return new ErrorResponseDto(exception.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(UNPROCESSABLE_ENTITY)
    public ErrorResponseDto handleValidationException(final Exception exception) {
        return new ErrorResponseDto(exception.getMessage(), LocalDateTime.now());
    }
}
