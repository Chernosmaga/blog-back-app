package com.example.service;

import com.example.dto.LikesCountResponseDto;
import com.example.dto.PostPaginationResponseDto;
import com.example.dto.PostRequestDto;
import com.example.dto.PostResponseDto;

import java.util.UUID;

public interface PostService {
    PostPaginationResponseDto getAll(String search, int pageNumber, int pageSize);

    PostResponseDto getById(UUID postId);

    PostResponseDto create(PostRequestDto post);

    PostResponseDto update(UUID postId, PostRequestDto post);

    void delete(UUID postId);

    LikesCountResponseDto incrementLike(UUID postId);
}
