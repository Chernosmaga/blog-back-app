package com.example.blog_back_app.post.dto;

import java.util.List;

public record PostPaginationResponseDto(List<PostResponseDto> posts, Boolean hasPrev, Boolean hasNext, Long lastPage) {}