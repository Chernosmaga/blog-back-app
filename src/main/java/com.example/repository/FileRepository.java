package com.example.repository;

import com.example.model.File;

import java.util.UUID;

public interface FileRepository {
    void save(UUID postId, String filename, byte[] content);

    File get(UUID postId);
}
