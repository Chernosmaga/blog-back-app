package com.example.blog_back_app.utilities;

import com.example.blog_back_app.exception.NotFoundException;
import com.example.blog_back_app.exception.ValidationException;
import com.example.blog_back_app.post.model.Post;
import com.example.blog_back_app.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UtilService {
    private final PostRepository postRepository;

    public void postExists(UUID postId) {
        if (!postRepository.existsById(postId)) throw new NotFoundException("Requested post wasn't found");
    }

    public Post findById(UUID postId) {
        return postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Requested post wasn't found"));
    }

    public void postIdsAreEquals(UUID firstPostId, UUID secondPostId) {
        if (!firstPostId.equals(secondPostId)) throw new ValidationException("The provided identifiers do not match");
    }
}