package com.example.blog_back_app.exception.controller;

import com.example.blog_back_app.exception.NotFoundException;
import com.example.blog_back_app.exception.ValidationException;
import com.example.blog_back_app.exception.dto.ErrorResponseDto;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(NOT_FOUND)
    public ErrorResponseDto handleNotFoundException(final Exception exception) {
        return new ErrorResponseDto(exception.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponseDto handleValidationException(final Exception exception) {

        String message;

        if (exception instanceof MethodArgumentNotValidException ex) {
            message = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        } else {
            message = exception.getMessage();
        }

        return new ErrorResponseDto(message, LocalDateTime.now());
    }
}