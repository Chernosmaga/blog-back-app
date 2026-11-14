package com.example;

import com.example.configuration.TestConfiguration;
import com.example.dto.PostPaginationResponseDto;
import com.example.dto.PostRequestDto;
import com.example.dto.PostResponseDto;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.model.Post;
import com.example.repository.PostRepository;
import com.example.service.PostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class PostServiceImplTest {
    private final PostService postService;
    private final PostRepository postRepository;

    private final Post FIRST_MOCK_POST = new Post(UUID.randomUUID(),
            "First test post",
            "First test post description",
            0L,
            List.of("First tag", "Third tag"));
    private final Post SECOND_MOCK_POST = new Post(UUID.randomUUID(),
            "Second test post",
            "Second test post description",
            0L,
            List.of("Fourth tag", "Fifth tag"));
    private UUID firstMockPostId;
    private UUID secondMockPostId;

    @Autowired
    public PostServiceImplTest(PostService postService, PostRepository postRepository) {
        this.postService = postService;
        this.postRepository = postRepository;
    }

    @BeforeEach
    public void beforeEach() {
        firstMockPostId = postRepository.create(FIRST_MOCK_POST);
        postRepository.addTags(firstMockPostId, FIRST_MOCK_POST.getTags());

        secondMockPostId = postRepository.create(SECOND_MOCK_POST);
        postRepository.addTags(secondMockPostId, SECOND_MOCK_POST.getTags());
    }

    @AfterEach
    public void afterEach() {
        postRepository.delete(firstMockPostId);
        postRepository.delete(secondMockPostId);
    }

    @Test
    public void getById_shouldReturnPostById() {
        PostResponseDto post = postService.getById(firstMockPostId);

        assertNotNull(post);
        assertEquals("First test post", post.getTitle());
        assertEquals("First test post description", post.getText());
        assertEquals(0L, post.getLikesCount());
        assertEquals(List.of("First tag", "Third tag"), post.getTags());
    }

    @Test
    public void getById_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> postService.getById(UUID.randomUUID()));
    }

    @Test
    public void create_shouldCreatePost() {
        PostRequestDto postRequestDto = new PostRequestDto(
                "First test post",
                "First test post description",
                List.of("First tag"));
        PostResponseDto postResponseDto = postService.create(postRequestDto);

        assertNotNull(postResponseDto);
        assertEquals("First test post", postResponseDto.getTitle());
        assertEquals("First test post description", postResponseDto.getText());
        assertEquals(0L, postResponseDto.getLikesCount());
        assertEquals(List.of("First tag"), postResponseDto.getTags());
        assertEquals(0L, postResponseDto.getCommentsCount());
    }

    @Test
    public void create_shouldThrowValidationExceptionForTitle() {
        PostRequestDto postRequestDto =
                new PostRequestDto(null, "First test post description", List.of("First tag"));

        assertThrows(ValidationException.class, () -> postService.create(postRequestDto));
    }

    @Test
    public void create_shouldThrowValidationExceptionForText() {
        PostRequestDto postRequestDto =
                new PostRequestDto("First test post", "", List.of("First tag"));

        assertThrows(ValidationException.class, () -> postService.create(postRequestDto));
    }

    @Test
    public void create_shouldThrowValidationExceptionForTags() {
        PostRequestDto postRequestDto =
                new PostRequestDto("First test post", "First test post description", List.of());

        assertThrows(ValidationException.class, () -> postService.create(postRequestDto));
    }

    @Test
    public void getAll_shouldReturnAllExistingPosts() {
        PostPaginationResponseDto paginationResponseDto =
                postService.getAll("", 0, 10);

        assertFalse(paginationResponseDto.getPosts().isEmpty());
        assertEquals(0, paginationResponseDto.getLastPage());
    }

    @Test
    public void getAll_shouldReturnNoPosts() {
        PostPaginationResponseDto paginationResponseDto =
                postService.getAll("no data", 0, 10);

        assertTrue(paginationResponseDto.getPosts().isEmpty());
        assertEquals(0, paginationResponseDto.getLastPage());
        assertEquals(false, paginationResponseDto.getHasNext());
        assertEquals(false, paginationResponseDto.getHasPrev());
    }

    @Test
    public void update_shouldUpdatePost() {
        FIRST_MOCK_POST.setTitle("Updated title");
        FIRST_MOCK_POST.setText("Updated text");
        FIRST_MOCK_POST.setTags(List.of("Updated tag"));

        PostResponseDto responseDto = postService.update(firstMockPostId,
                new PostRequestDto(FIRST_MOCK_POST.getTitle(), FIRST_MOCK_POST.getText(), FIRST_MOCK_POST.getTags()));

        assertNotNull(responseDto);
        assertEquals("Updated title", responseDto.getTitle());
        assertEquals("Updated text", responseDto.getText());
        assertEquals(List.of("Updated tag"), responseDto.getTags());
    }

    @Test
    public void update_shouldThrowExceptionForNotExistingPost() {
        assertThrows(NotFoundException.class, () -> postService.update(UUID.randomUUID(),
                new PostRequestDto(
                        FIRST_MOCK_POST.getTitle(),
                        FIRST_MOCK_POST.getText(),
                        FIRST_MOCK_POST.getTags())));
    }

    @Test
    public void update_shouldThrowExceptionForTitle() {
        FIRST_MOCK_POST.setTitle("");
        FIRST_MOCK_POST.setText("Updated text");
        FIRST_MOCK_POST.setTags(List.of("Updated tag"));

        assertThrows(ValidationException.class, () -> postService.update(firstMockPostId,
                new PostRequestDto(FIRST_MOCK_POST.getTitle(), FIRST_MOCK_POST.getText(), FIRST_MOCK_POST.getTags())));
    }

    @Test
    public void update_shouldThrowExceptionForText() {
        FIRST_MOCK_POST.setTitle("Updated title");
        FIRST_MOCK_POST.setText(null);
        FIRST_MOCK_POST.setTags(List.of("Updated tag"));

        assertThrows(ValidationException.class, () -> postService.update(firstMockPostId,
                new PostRequestDto(FIRST_MOCK_POST.getTitle(), FIRST_MOCK_POST.getText(), FIRST_MOCK_POST.getTags())));
    }

    @Test
    public void update_shouldThrowExceptionForTags() {
        FIRST_MOCK_POST.setTitle("Updated title");
        FIRST_MOCK_POST.setText("Updated text");
        FIRST_MOCK_POST.setTags(List.of());

        assertThrows(ValidationException.class, () -> postService.update(firstMockPostId,
                new PostRequestDto(FIRST_MOCK_POST.getTitle(), FIRST_MOCK_POST.getText(), FIRST_MOCK_POST.getTags())));
    }

    @Test
    public void delete_shouldThrowExceptionForDeletedPost() {
        PostRequestDto postRequestDto = new PostRequestDto(
                "First test post",
                "First test post description",
                List.of("First tag"));
        PostResponseDto postResponseDto = postService.create(postRequestDto);
        postService.delete(postResponseDto.getId());

        assertThrows(NotFoundException.class, () -> postService.getById(postResponseDto.getId()));
    }

    @Test
    public void delete_shouldThrowExceptionForNotExistingPost() {
        assertThrows(NotFoundException.class, () -> postService.getById(UUID.randomUUID()));
    }

    @Test
    public void incrementLike_shouldIncrementLikesByTwo() {
        postService.incrementLike(firstMockPostId);
        postService.incrementLike(firstMockPostId);
        PostResponseDto responseDto = postService.getById(firstMockPostId);

        assertEquals(2L, responseDto.getLikesCount());
    }

    @Test
    public void incrementLike_shouldThrowExceptionForNotFoundPost() {
        assertThrows(NotFoundException.class, () -> postService.incrementLike(UUID.randomUUID()));
    }
}
