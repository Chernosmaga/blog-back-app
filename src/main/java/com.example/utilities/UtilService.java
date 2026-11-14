package com.example.utilities;

import com.example.exception.NotFoundException;
import com.example.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UtilService {
    private final PostRepository postRepository;

    public void postExists(UUID postId) {
        try {
            postRepository.getById(postId);
        } catch (Exception exception) {
            throw new NotFoundException("Requested post was not found");
        }
    }
}
