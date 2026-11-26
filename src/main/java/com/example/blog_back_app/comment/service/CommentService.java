package com.example.blog_back_app.comment.service;

import com.example.blog_back_app.comment.dto.CommentRequestDto;
import com.example.blog_back_app.comment.dto.CommentResponseDto;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    List<CommentResponseDto> getAllByPostId(UUID postId);

    CommentResponseDto getByIdAndPostId(UUID commentId, UUID postId);

    CommentResponseDto create(UUID postId, CommentRequestDto comment);

    CommentResponseDto update(UUID postId, UUID commentId, CommentRequestDto comment);

    void delete(UUID postId, UUID commentId);
}
