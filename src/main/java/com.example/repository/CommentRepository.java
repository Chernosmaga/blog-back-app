package com.example.repository;

import com.example.model.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentRepository {
    List<Comment> getAllByPostId(UUID postId);

    Comment getByIdAndPostId(UUID commentId, UUID postId);

    UUID create(UUID postId, Comment comment);

    void update(UUID postId, UUID commentId, String text);

    void delete(UUID postId, UUID commentId);

    long countForPost(UUID postId);
}
