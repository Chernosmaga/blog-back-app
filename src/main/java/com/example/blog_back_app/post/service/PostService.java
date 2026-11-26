package com.example.blog_back_app.post.service;

import com.example.blog_back_app.post.dto.LikesCountResponseDto;
import com.example.blog_back_app.post.dto.PostPaginationResponseDto;
import com.example.blog_back_app.post.dto.PostRequestDto;
import com.example.blog_back_app.post.dto.PostResponseDto;

import java.util.UUID;

public interface PostService {
    PostPaginationResponseDto getAll(String search, int pageNumber, int pageSize);

    PostResponseDto getById(UUID postId);

    PostResponseDto create(PostRequestDto post);

    PostResponseDto update(UUID postId, PostRequestDto post);

    void delete(UUID postId);

    LikesCountResponseDto incrementLike(UUID postId);
}
