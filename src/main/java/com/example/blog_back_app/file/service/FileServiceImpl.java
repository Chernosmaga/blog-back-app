package com.example.blog_back_app.file.service;

import com.example.blog_back_app.exception.NotFoundException;
import com.example.blog_back_app.exception.ValidationException;
import com.example.blog_back_app.file.model.File;
import com.example.blog_back_app.file.repository.FileRepository;
import com.example.blog_back_app.utilities.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final UtilService utilService;

    @Override
    public void save(UUID postId, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new ValidationException("File is empty");
        utilService.postExists(postId);

        File toSave = new File();
        toSave.setContent(file.getBytes());
        toSave.setFilename(UUID.randomUUID() + "_" + file.getOriginalFilename());
        toSave.setPost(utilService.findById(postId));

        fileRepository.save(toSave);
    }

    @Override
    public File get(UUID postId) {
        utilService.postExists(postId);
        return fileRepository.findByPostId(postId).orElseThrow(() -> new NotFoundException("File wasn't found"));
    }
}
