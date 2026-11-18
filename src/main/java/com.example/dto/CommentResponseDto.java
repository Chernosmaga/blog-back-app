package com.example.dto;

import java.util.UUID;

public record CommentResponseDto(UUID id, UUID postId, String text) {}
