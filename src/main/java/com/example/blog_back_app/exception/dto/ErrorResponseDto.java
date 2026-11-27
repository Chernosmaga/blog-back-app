package com.example.blog_back_app.exception.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ErrorResponseDto(String message,
                               @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime timestamp) {}