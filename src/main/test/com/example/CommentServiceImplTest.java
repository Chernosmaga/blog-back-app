package com.example;

import com.example.configuration.TestConfiguration;
import com.example.dto.CommentRequestDto;
import com.example.dto.CommentResponseDto;
import com.example.dto.PostResponseDto;
import com.example.exception.DataViolationException;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.model.Post;
import com.example.repository.PostRepository;
import com.example.service.CommentService;
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
public class CommentServiceImplTest {
    private final PostService postService;
    private final PostRepository postRepository;
    private final CommentService commentService;

    private UUID mockPostId;
    private final Post MOCK_POST = new Post(UUID.randomUUID(),
            "First test post",
            "First test post description",
            0L,
            List.of("First tag"));

    @Autowired
    public CommentServiceImplTest(PostService postService,
                                  PostRepository postRepository,
                                  CommentService commentService) {
        this.postService = postService;
        this.postRepository = postRepository;
        this.commentService = commentService;
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
    public void create_shouldCreateComment() {
        CommentResponseDto commentResponseDto =
                commentService.create(mockPostId, new CommentRequestDto(mockPostId, "First comment"));

        assertNotNull(commentResponseDto);
        assertEquals(mockPostId, commentResponseDto.getPostId());
        assertEquals("First comment", commentResponseDto.getText());
    }

    @Test
    public void create_shouldThrowExceptionIfPostNotFound() {
        UUID uuid = UUID.randomUUID();
        assertThrows(NotFoundException.class,
                () -> commentService.create(uuid, new CommentRequestDto(uuid, "First comment")));
    }

    @Test
    public void create_shouldThrowExceptionIfTextIsEmpty() {
        assertThrows(ValidationException.class,
                () -> commentService.create(mockPostId, new CommentRequestDto(mockPostId, "")));
    }

    @Test
    public void create_shouldThrowExceptionIfPostIdIsNull() {
        assertThrows(ValidationException.class,
                () -> commentService.create(mockPostId, new CommentRequestDto(null, "Updated comment")));
    }

    @Test
    public void getByIdAndPostId_shouldReturnComment() {
        CommentResponseDto commentResponseDto =
                commentService.create(mockPostId, new CommentRequestDto(mockPostId, "Second comment"));
        CommentResponseDto returnedComment =
                commentService.getByIdAndPostId(commentResponseDto.getId(), commentResponseDto.getPostId());

        assertNotNull(returnedComment);
        assertEquals("Second comment", returnedComment.getText());
        assertEquals(mockPostId, returnedComment.getPostId());
    }

    @Test
    public void getByIdAndPostId_shouldThrowExceptionIfPostNotFound() {
        CommentResponseDto commentResponseDto =
                commentService.create(mockPostId, new CommentRequestDto(mockPostId, "Second comment"));

        assertThrows(NotFoundException.class,
                () -> commentService.getByIdAndPostId(commentResponseDto.getId(), UUID.randomUUID()));
    }

    @Test
    public void getByIdAndPostId_shouldThrowExceptionIfCommentNotFound() {
        assertThrows(NotFoundException.class,
                () -> commentService.getByIdAndPostId(UUID.randomUUID(), mockPostId));
    }

    @Test
    public void getAllByPostId_shouldReturnTwoComments() {
        commentService.create(mockPostId, new CommentRequestDto(mockPostId, "First comment"));
        commentService.create(mockPostId, new CommentRequestDto(mockPostId, "Second comment"));
        List<CommentResponseDto> comments = commentService.getAllByPostId(mockPostId);

        assertFalse(comments.isEmpty());
        assertEquals(2, comments.size());
        assertEquals("Second comment", comments.get(1).getText());
    }

    @Test
    public void getAllByPostId_shouldThrowExceptionIfPostNotFound() {
        assertThrows(NotFoundException.class, () -> commentService.getAllByPostId(UUID.randomUUID()));
    }

    @Test
    public void getAllByPostId_shouldReturnNoComments() {
        List<CommentResponseDto> comments = commentService.getAllByPostId(mockPostId);

        assertTrue(comments.isEmpty());
    }

    @Test
    public void update_shouldUpdateComment() {
        CommentResponseDto commentResponseDto =
                commentService.create(mockPostId, new CommentRequestDto(mockPostId, "First comment"));
        CommentResponseDto updated = commentService.update(mockPostId, commentResponseDto.getId(),
                new CommentRequestDto(mockPostId, "Updated comment"));

        assertEquals("Updated comment", updated.getText());
        assertEquals(mockPostId, updated.getPostId());
        assertEquals(commentResponseDto.getId(), updated.getId());
    }

    @Test
    public void update_shouldThrowExceptionIfPostNotFound() {
        CommentResponseDto commentResponseDto =
                commentService.create(mockPostId, new CommentRequestDto(mockPostId, "First comment"));
        UUID uuid = UUID.randomUUID();

        assertThrows(NotFoundException.class, () -> commentService.update(uuid, commentResponseDto.getId(),
                new CommentRequestDto(uuid, "Updated comment")));
    }

    @Test
    public void update_shouldThrowExceptionIfCommentNotFound() {
        assertThrows(DataViolationException.class, () -> commentService.update(mockPostId, UUID.randomUUID(),
                new CommentRequestDto(mockPostId, "Updated comment")));
    }

    @Test
    public void update_shouldThrowExceptionIfTextIsEmpty() {
        CommentResponseDto commentResponseDto =
                commentService.create(mockPostId, new CommentRequestDto(mockPostId, "First comment"));

        assertThrows(ValidationException.class, () -> commentService.update(mockPostId, commentResponseDto.getId(),
                new CommentRequestDto(mockPostId, "")));
    }

    @Test
    public void update_shouldThrowExceptionIfPostIdIsNull() {
        CommentResponseDto commentResponseDto =
                commentService.create(mockPostId, new CommentRequestDto(mockPostId, "First comment"));

        assertThrows(ValidationException.class, () -> commentService.update(mockPostId, commentResponseDto.getId(),
                new CommentRequestDto(null, "Updated comment")));
    }

    @Test
    public void delete_shouldDeleteComment() {
        CommentResponseDto commentResponseDto =
                commentService.create(mockPostId, new CommentRequestDto(mockPostId, "First comment"));
        commentService.delete(mockPostId, commentResponseDto.getId());
        PostResponseDto post = postService.getById(mockPostId);

        assertEquals(0, (long) post.getCommentsCount());
    }

    @Test
    public void delete_shouldThrowExceptionIfCommentNotFound() {
        assertThrows(NotFoundException.class, () -> commentService.delete(mockPostId, UUID.randomUUID()));
    }
}
