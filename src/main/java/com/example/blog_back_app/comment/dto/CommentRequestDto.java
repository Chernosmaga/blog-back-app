package com.example.blog_back_app.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommentRequestDto(@NotNull(message = "Post id is required") UUID postId,
                                @NotBlank(message = "Required text field is not present") String text) {}