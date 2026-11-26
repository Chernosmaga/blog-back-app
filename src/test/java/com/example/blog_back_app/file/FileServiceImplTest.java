package com.example.blog_back_app.file;

import com.example.blog_back_app.exception.NotFoundException;
import com.example.blog_back_app.exception.ValidationException;
import com.example.blog_back_app.file.model.File;
import com.example.blog_back_app.file.repository.FileRepository;
import com.example.blog_back_app.file.service.FileService;
import com.example.blog_back_app.post.model.Post;
import com.example.blog_back_app.post.repository.PostRepository;
import com.example.blog_back_app.post.service.PostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@SpringBootTest
class FileServiceImplTest {
    private final FileService fileService;
    @MockitoSpyBean
    private final FileRepository fileRepository;
    private final PostRepository postRepository;
    private final PostService postService;

    private final Post MOCK_POST = new Post(null,
            "First test post",
            "First test post description",
            0L,
            List.of("First tag", "Third tag"),
            List.of());

    @Autowired
    public FileServiceImplTest(FileService fileService,
                               FileRepository fileRepository,
                               PostRepository postRepository,
                               PostService postService) {
        this.fileService = fileService;
        this.fileRepository = fileRepository;
        this.postRepository = postRepository;
        this.postService = postService;
    }

    @AfterEach
    public void afterEach() {
        fileRepository.deleteAll();
        postRepository.deleteAll();
    }

    @Test
    public void save_shouldSaveAndReturnFile() throws IOException {
        var post = postRepository.save(MOCK_POST);
        var postId = post.getId();
        var mockMultipartFile = new MockMultipartFile("file",
                "test.jpg", "image/jpg", new byte[]{0, 1, 2, 3});
        fileService.save(postId, mockMultipartFile);
        var returned = fileService.get(postId);

        assertNotNull(returned);
        assertArrayEquals(new byte[]{0, 1, 2, 3}, returned.getContent());
        assertEquals(postId, returned.getPost().getId());

        var fileArgumentCaptor = ArgumentCaptor.forClass(File.class);
        verify(fileRepository).save(fileArgumentCaptor.capture());
        var saved = fileArgumentCaptor.getValue();

        assertTrue(saved.getFilename().contains("test.jpg"));
        assertEquals(postId, saved.getPost().getId());
    }

    @Test
    public void save_shouldThrowExceptionIfPostNotFound() {
        var mockMultipartFile = new MockMultipartFile("file",
                "test.jpg", "image/jpg", new byte[]{0, 1, 2, 3});

        assertThrows(NotFoundException.class,
                () -> fileService.save(UUID.randomUUID(), mockMultipartFile));
    }

    @Test
    public void save_shouldThrowExceptionIfFileIsEmpty() {
        var mockMultipartFile = new MockMultipartFile("file",
                "test.jpg", "image/jpg", new byte[0]);

        assertThrows(ValidationException.class,
                () -> fileService.save(UUID.randomUUID(), mockMultipartFile));
    }

    @Test
    public void get_shouldThrowExceptionIfPostNotFound() {
        assertThrows(NotFoundException.class, () -> fileService.get(UUID.randomUUID()));
    }

    @Test
    public void shouldDeleteCreatedFilesBeforeDeletingPost() throws IOException {
        var post = postRepository.save(MOCK_POST);
        var postId = post.getId();
        var mockMultipartFile = new MockMultipartFile("file",
                "test.jpg", "image/jpg", new byte[]{0, 1, 2, 3});
        fileService.save(postId, mockMultipartFile);
        postService.delete(postId);

        assertThrows(NotFoundException.class, () -> fileService.get(postId));

        var postIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(fileRepository).deleteAllByPostId(postIdArgumentCaptor.capture());

        assertEquals(postId, postIdArgumentCaptor.getValue());
    }
}