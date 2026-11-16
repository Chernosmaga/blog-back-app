package com.example;

import com.example.configuration.RootTestConfiguration;
import com.example.dto.CommentRequestDto;
import com.example.dto.CommentResponseDto;
import com.example.dto.PostResponseDto;
import com.example.exception.DataViolationException;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.model.Post;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import com.example.service.CommentService;
import com.example.service.PostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringJUnitConfig(classes = RootTestConfiguration.class)
public class CommentServiceImplTest {
    private final PostService postService;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentService commentService;

    @Autowired
    public CommentServiceImplTest(PostService postService,
                                  CommentRepository commentRepository,
                                  PostRepository postRepository,
                                  CommentService commentService) {
        this.postService = postService;
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.commentService = commentService;
    }

    @BeforeEach
    public void beforeEach() {
        commentRepository.deleteAll();

        Post mockPost = new Post(UUID.randomUUID(),
                "First test post",
                "First test post description",
                0L,
                List.of("First tag"));

        var mockPostId = postRepository.create(mockPost);
        postRepository.addTags(mockPostId, mockPost.getTags());
    }

    @AfterEach
    public void afterEach() {
        postRepository.deleteAll();
    }

    @Test
    public void create_shouldCreateComment() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(),
                        new CommentRequestDto(firstPost.getId(), "First comment"));

        assertNotNull(commentResponseDto);
        assertEquals(firstPost.getId(), commentResponseDto.getPostId());
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
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();

        assertThrows(ValidationException.class,
                () -> commentService.create(firstPost.getId(), new CommentRequestDto(firstPost.getId(), "")));
    }

    @Test
    public void create_shouldThrowExceptionIfPostIdIsNull() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();

        assertThrows(ValidationException.class,
                () -> commentService.create(firstPost.getId(),
                        new CommentRequestDto(null, "Updated comment")));
    }

    @Test
    public void getByIdAndPostId_shouldReturnComment() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(),
                        new CommentRequestDto(firstPost.getId(), "Second comment"));
        CommentResponseDto returnedComment =
                commentService.getByIdAndPostId(commentResponseDto.getId(), commentResponseDto.getPostId());

        assertNotNull(returnedComment);
        assertEquals("Second comment", returnedComment.getText());
        assertEquals(firstPost.getId(), returnedComment.getPostId());
    }

    @Test
    public void getByIdAndPostId_shouldThrowExceptionIfPostNotFound() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(),
                        new CommentRequestDto(firstPost.getId(), "Second comment"));

        assertThrows(NotFoundException.class,
                () -> commentService.getByIdAndPostId(commentResponseDto.getId(), UUID.randomUUID()));
    }

    @Test
    public void getByIdAndPostId_shouldThrowExceptionIfCommentNotFound() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();

        assertThrows(NotFoundException.class,
                () -> commentService.getByIdAndPostId(UUID.randomUUID(), firstPost.getId()));
    }

    @Test
    public void getAllByPostId_shouldReturnTwoComments() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        commentService.create(firstPost.getId(), new CommentRequestDto(firstPost.getId(), "First comment"));
        commentService.create(firstPost.getId(), new CommentRequestDto(firstPost.getId(), "Second comment"));
        List<CommentResponseDto> comments = commentService.getAllByPostId(firstPost.getId());

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
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        List<CommentResponseDto> comments = commentService.getAllByPostId(firstPost.getId());

        assertTrue(comments.isEmpty());
    }

    @Test
    public void update_shouldUpdateComment() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(),
                        new CommentRequestDto(firstPost.getId(), "First comment"));
        CommentResponseDto updated = commentService.update(firstPost.getId(), commentResponseDto.getId(),
                new CommentRequestDto(firstPost.getId(), "Updated comment"));

        assertEquals("Updated comment", updated.getText());
        assertEquals(firstPost.getId(), updated.getPostId());
        assertEquals(commentResponseDto.getId(), updated.getId());
    }

    @Test
    public void update_shouldThrowExceptionIfPostNotFound() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(),
                        new CommentRequestDto(firstPost.getId(), "First comment"));
        UUID uuid = UUID.randomUUID();

        assertThrows(NotFoundException.class, () -> commentService.update(uuid, commentResponseDto.getId(),
                new CommentRequestDto(uuid, "Updated comment")));
    }

    @Test
    public void update_shouldThrowExceptionIfCommentNotFound() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();

        assertThrows(DataViolationException.class, () -> commentService.update(firstPost.getId(), UUID.randomUUID(),
                new CommentRequestDto(firstPost.getId(), "Updated comment")));
    }

    @Test
    public void update_shouldThrowExceptionIfTextIsEmpty() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(), new CommentRequestDto(firstPost.getId(), "First comment"));

        assertThrows(ValidationException.class,
                () -> commentService.update(firstPost.getId(), commentResponseDto.getId(),
                        new CommentRequestDto(firstPost.getId(), "")));
    }

    @Test
    public void update_shouldThrowExceptionIfPostIdIsNull() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(),
                        new CommentRequestDto(firstPost.getId(), "First comment"));

        assertThrows(ValidationException.class,
                () -> commentService.update(firstPost.getId(), commentResponseDto.getId(),
                new CommentRequestDto(null, "Updated comment")));
    }

    @Test
    public void delete_shouldDeleteComment() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(),
                        new CommentRequestDto(firstPost.getId(), "First comment"));
        commentService.delete(firstPost.getId(), commentResponseDto.getId());
        PostResponseDto post = postService.getById(firstPost.getId());

        assertEquals(0, (long) post.getCommentsCount());
    }

    @Test
    public void delete_shouldThrowExceptionIfCommentNotFound() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();

        assertThrows(NotFoundException.class, () -> commentService.delete(firstPost.getId(), UUID.randomUUID()));
    }

    @Test
    public void shouldReturnCorrectCommentsCount() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        commentService.create(firstPost.getId(), new CommentRequestDto(firstPost.getId(), "First comment"));
        commentService.create(firstPost.getId(), new CommentRequestDto(firstPost.getId(), "Second comment"));
        var returnedPost = postService.getById(firstPost.getId());

        assertEquals(2, returnedPost.getCommentsCount());
    }
}
