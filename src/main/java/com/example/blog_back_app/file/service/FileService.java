package com.example.blog_back_app.file.service;

import com.example.blog_back_app.file.model.File;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface FileService {
    void save(UUID postId, MultipartFile file) throws IOException;

    File get(UUID postId);
}
