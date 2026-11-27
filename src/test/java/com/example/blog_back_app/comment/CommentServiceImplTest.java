package com.example.blog_back_app.comment;

import com.example.blog_back_app.comment.dto.CommentRequestDto;
import com.example.blog_back_app.comment.model.Comment;
import com.example.blog_back_app.comment.repository.CommentRepository;
import com.example.blog_back_app.comment.service.CommentService;
import com.example.blog_back_app.exception.NotFoundException;
import com.example.blog_back_app.exception.ValidationException;
import com.example.blog_back_app.post.model.Post;
import com.example.blog_back_app.post.repository.PostRepository;
import com.example.blog_back_app.post.service.PostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class CommentServiceImplTest {
    @MockitoSpyBean
    private final CommentRepository commentRepository;
    private final CommentService commentService;
    private final PostRepository postRepository;
    private final PostService postService;

    @Autowired
    public CommentServiceImplTest(CommentRepository commentRepository,
                                  CommentService commentService,
                                  PostRepository postRepository,
                                  PostService postService) {
        this.commentRepository = commentRepository;
        this.commentService = commentService;
        this.postRepository = postRepository;
        this.postService = postService;
    }

    @BeforeEach
    public void beforeEach() {
        commentRepository.deleteAll();

        var mockPost = new Post(null,
                "First test post",
                "First test post description",
                0L,
                List.of("First tag"),
                List.of());

        postRepository.save(mockPost);
    }

    @AfterEach
    public void afterEach() {
        postRepository.deleteAll();
    }

    @Test
    public void create_shouldCreateComment() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();
        var commentResponseDto = commentService.create(firstPost.id(),
                new CommentRequestDto(firstPost.id(), "First comment"));

        assertNotNull(commentResponseDto);
        assertEquals(firstPost.id(), commentResponseDto.postId());
        assertEquals("First comment", commentResponseDto.text());

        var commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentArgumentCaptor.capture());
        var saved = commentArgumentCaptor.getValue();

        assertEquals("First comment", saved.getText());
        assertEquals(firstPost.id(), saved.getPost().getId());
    }

    @Test
    public void create_shouldThrowExceptionIfPostNotFound() {
        UUID uuid = UUID.randomUUID();
        assertThrows(NotFoundException.class,
                () -> commentService.create(uuid, new CommentRequestDto(uuid, "First comment")));
    }

    @Test
    public void create_shouldThrowExceptionIfPostIdIsNull() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();

        assertThrows(ValidationException.class,
                () -> commentService.create(firstPost.id(),
                        new CommentRequestDto(null, "Updated comment")));
    }

    @Test
    public void create_shouldThrowExceptionIfPostIdsAreNotEquals() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();

        assertThrows(ValidationException.class,
                () -> commentService.create(firstPost.id(),
                        new CommentRequestDto(UUID.randomUUID(), "New comment")));
    }

    @Test
    public void getByIdAndPostId_shouldReturnComment() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();
        var commentResponseDto = commentService.create(firstPost.id(),
                new CommentRequestDto(firstPost.id(), "Second comment"));
        var returnedComment = commentService.getByIdAndPostId(commentResponseDto.id(), commentResponseDto.postId());

        assertNotNull(returnedComment);
        assertEquals("Second comment", returnedComment.text());
        assertEquals(firstPost.id(), returnedComment.postId());

        var postIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        var commentIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(commentRepository).findByIdAndPostId(commentIdArgumentCaptor.capture(), postIdArgumentCaptor.capture());

        assertEquals(firstPost.id(), postIdArgumentCaptor.getValue());
        assertEquals(commentResponseDto.id(), commentIdArgumentCaptor.getValue());
    }

    @Test
    public void getByIdAndPostId_shouldThrowExceptionIfPostNotFound() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();
        var commentResponseDto = commentService.create(firstPost.id(),
                new CommentRequestDto(firstPost.id(), "Second comment"));

        assertThrows(NotFoundException.class,
                () -> commentService.getByIdAndPostId(commentResponseDto.id(), UUID.randomUUID()));
    }

    @Test
    public void getByIdAndPostId_shouldThrowExceptionIfCommentNotFound() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();

        assertThrows(NotFoundException.class,
                () -> commentService.getByIdAndPostId(UUID.randomUUID(), firstPost.id()));
    }

    @Test
    public void getAllByPostId_shouldReturnTwoComments() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();
        commentService.create(firstPost.id(), new CommentRequestDto(firstPost.id(), "First comment"));
        commentService.create(firstPost.id(), new CommentRequestDto(firstPost.id(), "Second comment"));
        var comments = commentService.getAllByPostId(firstPost.id());

        assertFalse(comments.isEmpty());
        assertEquals(2, comments.size());
        assertEquals("Second comment", comments.get(1).text());

        var postIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(commentRepository).findAllByPostId(postIdArgumentCaptor.capture());

        assertEquals(firstPost.id(), postIdArgumentCaptor.getValue());
    }

    @Test
    public void getAllByPostId_shouldThrowExceptionIfPostNotFound() {
        assertThrows(NotFoundException.class, () -> commentService.getAllByPostId(UUID.randomUUID()));
    }

    @Test
    public void getAllByPostId_shouldReturnNoComments() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();
        var comments = commentService.getAllByPostId(firstPost.id());

        assertTrue(comments.isEmpty());

        var postIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(commentRepository).findAllByPostId(postIdArgumentCaptor.capture());

        assertEquals(firstPost.id(), postIdArgumentCaptor.getValue());
    }

    @Test
    public void update_shouldUpdateComment() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();
        var commentResponseDto = commentService.create(firstPost.id(),
                new CommentRequestDto(firstPost.id(), "First comment"));
        var updated = commentService.update(firstPost.id(), commentResponseDto.id(),
                new CommentRequestDto(firstPost.id(), "Updated comment"));

        assertEquals("Updated comment", updated.text());
        assertEquals(firstPost.id(), updated.postId());
        assertEquals(commentResponseDto.id(), updated.id());

        var commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository, times(2)).save(commentArgumentCaptor.capture());
        var saved = commentArgumentCaptor.getAllValues().get(1);

        assertEquals("Updated comment", saved.getText());
        assertEquals(firstPost.id(), saved.getPost().getId());
    }

    @Test
    public void update_shouldThrowExceptionIfPostNotFound() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();
        var commentResponseDto = commentService.create(firstPost.id(),
                new CommentRequestDto(firstPost.id(), "First comment"));
        var uuid = UUID.randomUUID();

        assertThrows(NotFoundException.class, () -> commentService.update(uuid, commentResponseDto.id(),
                new CommentRequestDto(uuid, "Updated comment")));
    }

    @Test
    public void update_shouldThrowExceptionIfCommentNotFound() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();

        assertThrows(NotFoundException.class, () -> commentService.update(firstPost.id(), UUID.randomUUID(),
                new CommentRequestDto(firstPost.id(), "Updated comment")));
    }

    @Test
    public void update_shouldThrowExceptionIfPostIdIsNull() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();
        var commentResponseDto = commentService.create(firstPost.id(),
                new CommentRequestDto(firstPost.id(), "First comment"));

        assertThrows(ValidationException.class,
                () -> commentService.update(firstPost.id(), commentResponseDto.id(),
                        new CommentRequestDto(null, "Updated comment")));
    }

    @Test
    public void update_shouldThrowExceptionIfPostIdsAreNotEquals() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();
        var commentResponseDto = commentService.create(firstPost.id(),
                new CommentRequestDto(firstPost.id(), "First comment"));

        assertThrows(ValidationException.class,
                () -> commentService.update(firstPost.id(), commentResponseDto.id(),
                        new CommentRequestDto(UUID.randomUUID(), "Updated comment")));
    }

    @Test
    public void delete_shouldDeleteComment() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();
        var commentResponseDto = commentService.create(firstPost.id(),
                new CommentRequestDto(firstPost.id(), "First comment"));
        commentService.delete(firstPost.id(), commentResponseDto.id());
        var post = postService.getById(firstPost.id());

        assertEquals(0, (long) post.commentsCount());

        var commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).delete(commentArgumentCaptor.capture());
        var deleted = commentArgumentCaptor.getValue();

        assertEquals("First comment", deleted.getText());
        assertEquals(firstPost.id(), deleted.getPost().getId());
    }

    @Test
    public void delete_shouldThrowExceptionIfCommentNotFound() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();

        assertThrows(NotFoundException.class, () -> commentService.delete(firstPost.id(), UUID.randomUUID()));
    }

    @Test
    public void shouldReturnCorrectCommentsCount() {
        var postsPagination = postService.getAll("", 0, 10);
        var firstPost = postsPagination.posts().getFirst();
        commentService.create(firstPost.id(), new CommentRequestDto(firstPost.id(), "First comment"));
        commentService.create(firstPost.id(), new CommentRequestDto(firstPost.id(), "Second comment"));
        var returnedPost = postService.getById(firstPost.id());

        assertEquals(2, returnedPost.commentsCount());

        var commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository, times(2)).save(commentArgumentCaptor.capture());
        var saved = commentArgumentCaptor.getAllValues().get(1);

        assertEquals("Second comment", saved.getText());
        assertEquals(firstPost.id(), saved.getPost().getId());
    }
}