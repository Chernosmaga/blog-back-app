package com.example.dto;

import java.util.List;

public record PostPaginationResponseDto(List<PostResponseDto> posts, Boolean hasPrev, Boolean hasNext, Long lastPage) {}
