package com.example.dto;

import java.util.List;
import java.util.UUID;

public record PostResponseDto(UUID id,
                              String title,
                              String text,
                              Long likesCount,
                              Long commentsCount,
                              List<String> tags) {

    public PostResponseDto withCommentsCount(Long newCommentsCount) {
        return new PostResponseDto(
                this.id,
                this.title,
                this.text,
                this.likesCount,
                newCommentsCount,
                this.tags
        );
    }
}
