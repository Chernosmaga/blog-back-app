package com.example.blog_back_app.comment.dto;

import java.util.UUID;

public record CommentResponseDto(UUID id, UUID postId, String text) {}