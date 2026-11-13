package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PostResponseDto {

    private UUID id;

    private String title;

    private String text;

    private Long likesCount;

    private Long commentsCount;

    private List<String> tags;
}
