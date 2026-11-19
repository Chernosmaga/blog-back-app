package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostRequestDto(@NotBlank(message = "Title is required but wasn't provided") String title,
                             @NotBlank(message = "Text field is required but not present") String text,
                             @Size(min = 1, message = "Tags are required but not provided") List<String> tags) {}
