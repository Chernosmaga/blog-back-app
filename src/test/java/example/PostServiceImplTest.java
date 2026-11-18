package example;

import com.example.dto.PostPaginationResponseDto;
import com.example.dto.PostRequestDto;
import com.example.dto.PostResponseDto;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.model.Post;
import com.example.repository.PostRepository;
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
public class PostServiceImplTest {
    private final PostService postService;
    private final PostRepository postRepository;

    @Autowired
    public PostServiceImplTest(PostService postService, PostRepository postRepository) {
        this.postService = postService;
        this.postRepository = postRepository;
    }

    @BeforeEach
    public void beforeEach() {
        postRepository.deleteAll();

        Post firstMockPost = new Post(UUID.randomUUID(),
                "First test post",
                "First test post description",
                0L,
                List.of("First tag", "Third tag"));
        Post secondMockPost = new Post(UUID.randomUUID(),
                "Second test post",
                "Second test post description",
                0L,
                List.of("Fourth tag", "Fifth tag"));

        var firstMockPostId = postRepository.create(firstMockPost);
        postRepository.addTags(firstMockPostId, firstMockPost.getTags());

        var secondMockPostId = postRepository.create(secondMockPost);
        postRepository.addTags(secondMockPostId, secondMockPost.getTags());
    }

    @AfterEach
    public void afterEach() {
        postRepository.deleteAll();
    }

    @Test
    public void getById_shouldReturnPostById() {
        var posts = postRepository.getAll("", 0, 10);
        PostResponseDto post = postService.getById(posts.stream()
                .filter(p -> "First test post".equals(p.getTitle())).toList().getFirst().getId());

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
    public void getAll_shouldReturnOnePost() {
        PostPaginationResponseDto paginationResponseDto =
                postService.getAll("First test post", 0, 10);

        assertFalse(paginationResponseDto.getPosts().isEmpty());
        assertEquals(1, paginationResponseDto.getPosts().size());
        assertEquals(false, paginationResponseDto.getHasNext());
        assertEquals(0, paginationResponseDto.getLastPage());
    }

    @Test
    public void getAll_shouldReturnTwoPosts() {
        PostPaginationResponseDto paginationResponseDto = postService.getAll("", 0, 10);

        assertFalse(paginationResponseDto.getPosts().isEmpty());
        assertEquals(2, paginationResponseDto.getPosts().size());
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
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        PostResponseDto responseDto = postService.update(firstPost.getId(),
                new PostRequestDto("Updated title", "Updated text", List.of("Updated tag")));

        assertNotNull(responseDto);
        assertEquals("Updated title", responseDto.getTitle());
        assertEquals("Updated text", responseDto.getText());
        assertEquals(List.of("Updated tag"), responseDto.getTags());
    }

    @Test
    public void update_shouldThrowExceptionForNotExistingPost() {
        assertThrows(NotFoundException.class, () -> postService.update(UUID.randomUUID(),
                new PostRequestDto("First post title", "First post text", List.of("First tag"))));
    }

    @Test
    public void update_shouldThrowExceptionForTitle() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();

        assertThrows(ValidationException.class, () -> postService.update(firstPost.getId(),
                new PostRequestDto("", firstPost.getText(), firstPost.getTags())));
    }

    @Test
    public void update_shouldThrowExceptionForText() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();

        assertThrows(ValidationException.class, () -> postService.update(firstPost.getId(),
                new PostRequestDto(firstPost.getTitle(), "", firstPost.getTags())));
    }

    @Test
    public void update_shouldThrowExceptionForTags() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();

        assertThrows(ValidationException.class, () -> postService.update(firstPost.getId(),
                new PostRequestDto(firstPost.getTitle(), firstPost.getText(), List.of())));
    }

    @Test
    public void delete_shouldThrowExceptionForDeletedPost() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        postService.delete(firstPost.getId());

        assertThrows(NotFoundException.class, () -> postService.getById(firstPost.getId()));
    }

    @Test
    public void delete_shouldThrowExceptionForNotExistingPost() {
        assertThrows(NotFoundException.class, () -> postService.getById(UUID.randomUUID()));
    }

    @Test
    public void incrementLike_shouldIncrementLikesByTwo() {
        var posts = postRepository.getAll("", 0, 10);
        var firstPost = posts.getFirst();
        postService.incrementLike(firstPost.getId());
        postService.incrementLike(firstPost.getId());
        PostResponseDto responseDto = postService.getById(firstPost.getId());

        assertEquals(2L, responseDto.getLikesCount());
    }

    @Test
    public void incrementLike_shouldThrowExceptionForNotFoundPost() {
        assertThrows(NotFoundException.class, () -> postService.incrementLike(UUID.randomUUID()));
    }
}
