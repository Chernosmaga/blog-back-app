package com.example.service;

import com.example.exception.FileUploadException;
import com.example.exception.ValidationException;
import com.example.model.File;
import com.example.repository.FileRepository;
import com.example.utilities.UtilService;
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
    public void save(UUID postId, MultipartFile file, String filename) throws IOException {
        if (file.isEmpty()) throw new ValidationException("File is empty");
        utilService.postExists(postId);

        String newFilename = UUID.randomUUID() + "_" + filename;
        byte[] content = file.getBytes();

        try {
            fileRepository.save(postId, newFilename, content);
        } catch (Exception exception) {
            throw new FileUploadException("Error occurred while trying to upload file");
        }
    }

    @Override
    public File get(UUID postId) {
        utilService.postExists(postId);
        return fileRepository.get(postId);
    }
}
