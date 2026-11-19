package com.example.service;

import com.example.model.File;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface FileService {
    void save(UUID postId, MultipartFile file) throws IOException;

    File get(UUID postId);
}
