package example;

import com.example.dto.CommentRequestDto;
import com.example.dto.CommentResponseDto;
import com.example.dto.PostResponseDto;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.model.Post;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import com.example.service.CommentService;
import com.example.service.PostService;
import example.configuration.RootTestConfiguration;
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
        assertEquals(firstPost.getId(), commentResponseDto.postId());
        assertEquals("First comment", commentResponseDto.text());
    }

    @Test
    public void create_shouldThrowExceptionIfPostNotFound() {
        UUID uuid = UUID.randomUUID();
        assertThrows(NotFoundException.class,
                () -> commentService.create(uuid, new CommentRequestDto(uuid, "First comment")));
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
    public void create_shouldThrowExceptionIfPostIdsAreNotEquals() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();

        assertThrows(ValidationException.class,
                () -> commentService.create(firstPost.getId(),
                        new CommentRequestDto(UUID.randomUUID(), "New comment")));
    }

    @Test
    public void getByIdAndPostId_shouldReturnComment() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(),
                        new CommentRequestDto(firstPost.getId(), "Second comment"));
        CommentResponseDto returnedComment =
                commentService.getByIdAndPostId(commentResponseDto.id(), commentResponseDto.postId());

        assertNotNull(returnedComment);
        assertEquals("Second comment", returnedComment.text());
        assertEquals(firstPost.getId(), returnedComment.postId());
    }

    @Test
    public void getByIdAndPostId_shouldThrowExceptionIfPostNotFound() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(),
                        new CommentRequestDto(firstPost.getId(), "Second comment"));

        assertThrows(NotFoundException.class,
                () -> commentService.getByIdAndPostId(commentResponseDto.id(), UUID.randomUUID()));
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
        assertEquals("Second comment", comments.get(1).text());
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
        CommentResponseDto updated = commentService.update(firstPost.getId(), commentResponseDto.id(),
                new CommentRequestDto(firstPost.getId(), "Updated comment"));

        assertEquals("Updated comment", updated.text());
        assertEquals(firstPost.getId(), updated.postId());
        assertEquals(commentResponseDto.id(), updated.id());
    }

    @Test
    public void update_shouldThrowExceptionIfPostNotFound() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(),
                        new CommentRequestDto(firstPost.getId(), "First comment"));
        UUID uuid = UUID.randomUUID();

        assertThrows(NotFoundException.class, () -> commentService.update(uuid, commentResponseDto.id(),
                new CommentRequestDto(uuid, "Updated comment")));
    }

    @Test
    public void update_shouldThrowExceptionIfCommentNotFound() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();

        assertThrows(NotFoundException.class, () -> commentService.update(firstPost.getId(), UUID.randomUUID(),
                new CommentRequestDto(firstPost.getId(), "Updated comment")));
    }

    @Test
    public void update_shouldThrowExceptionIfPostIdIsNull() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(),
                        new CommentRequestDto(firstPost.getId(), "First comment"));

        assertThrows(ValidationException.class,
                () -> commentService.update(firstPost.getId(), commentResponseDto.id(),
                new CommentRequestDto(null, "Updated comment")));
    }

    @Test
    public void update_shouldThrowExceptionIfPostIdsAreNotEquals() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(),
                        new CommentRequestDto(firstPost.getId(), "First comment"));

        assertThrows(ValidationException.class,
                () -> commentService.update(firstPost.getId(), commentResponseDto.id(),
                        new CommentRequestDto(UUID.randomUUID(), "Updated comment")));
    }

    @Test
    public void delete_shouldDeleteComment() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        CommentResponseDto commentResponseDto =
                commentService.create(firstPost.getId(),
                        new CommentRequestDto(firstPost.getId(), "First comment"));
        commentService.delete(firstPost.getId(), commentResponseDto.id());
        PostResponseDto post = postService.getById(firstPost.getId());

        assertEquals(0, (long) post.commentsCount());
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

        assertEquals(2, returnedPost.commentsCount());
    }
}
