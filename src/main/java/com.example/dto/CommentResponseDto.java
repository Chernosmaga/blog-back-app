package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CommentResponseDto {

    private UUID id;

    private UUID postId;

    private String text;
}
