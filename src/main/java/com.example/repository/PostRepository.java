package com.example.repository;

import com.example.model.Post;

import java.util.List;
import java.util.UUID;

public interface PostRepository {
    List<Post> getAll(String search, int pageNumber, int pageSize);

    long countTotalPages(String search);

    Post getById(UUID postId);

    UUID create(Post post);

    void update(UUID postId, String title, String text);

    void delete(UUID postId);

    long incrementLike(UUID postId);

    void addTags(UUID postId, List<String> tags);

    void deleteTags(UUID postId);
}
