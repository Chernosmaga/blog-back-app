package com.example.utilities;

import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UtilService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public void postExists(UUID postId) {
        try {
            postRepository.getById(postId);
        } catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("Requested post was not found");
        }
    }

    public void postIdsAreEquals(UUID firstPostId, UUID secondPostId) {
        if (!firstPostId.equals(secondPostId)) throw new ValidationException("The provided identifiers do not match");
    }

    public void commentExists(UUID commentId) {
        try {
            commentRepository.getById(commentId);
        } catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("Requested comment was not found");
        }
    }
}
