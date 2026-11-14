package com.example;

import com.example.configuration.TestConfiguration;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.model.File;
import com.example.model.Post;
import com.example.repository.PostRepository;
import com.example.service.FileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class FileServiceImplTest {
    private final FileService fileService;
    private final PostRepository postRepository;
    private UUID mockPostId;
    private final Post MOCK_POST = new Post(UUID.randomUUID(),
            "First test post",
            "First test post description",
            0L,
            List.of("First tag", "Third tag"));
    private final File MOCK_FILE =
            new File(UUID.randomUUID(), "test.jpg", new byte[]{0, 1, 2, 3}, mockPostId);

    @Autowired
    public FileServiceImplTest(FileService fileService, PostRepository postRepository) {
        this.fileService = fileService;
        this.postRepository = postRepository;
    }

    @BeforeEach
    public void beforeEach() {
        mockPostId = postRepository.create(MOCK_POST);
        postRepository.addTags(mockPostId, MOCK_POST.getTags());
    }

    @AfterEach
    public void afterEach() {
        postRepository.delete(mockPostId);
    }

    @Test
    public void save_shouldSaveAndReturnFile() throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
                MOCK_FILE.getFilename(), "image/jpg", MOCK_FILE.getContent());
        fileService.save(mockPostId, mockMultipartFile, MOCK_FILE.getFilename());
        File returned = fileService.get(mockPostId);

        assertNotNull(returned);
        assertArrayEquals(new byte[]{0, 1, 2, 3}, returned.getContent());
        assertEquals(mockPostId, returned.getPostId());
    }

    @Test
    public void save_shouldThrowExceptionIfPostNotFound() {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
                MOCK_FILE.getFilename(), "image/jpg", MOCK_FILE.getContent());

        assertThrows(NotFoundException.class,
                () -> fileService.save(UUID.randomUUID(), mockMultipartFile, MOCK_FILE.getFilename()));
    }

    @Test
    public void save_shouldThrowExceptionIfFileIsEmpty() {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
                MOCK_FILE.getFilename(), "image/jpg", new byte[0]);

        assertThrows(ValidationException.class,
                () -> fileService.save(UUID.randomUUID(), mockMultipartFile, MOCK_FILE.getFilename()));
    }

    @Test
    public void get_shouldThrowExceptionIfPostNotFound() {
        assertThrows(NotFoundException.class, () -> fileService.get(UUID.randomUUID()));
    }
}
