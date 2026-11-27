package com.example.blog_back_app.post;

import com.example.blog_back_app.exception.NotFoundException;
import com.example.blog_back_app.post.dto.PostRequestDto;
import com.example.blog_back_app.post.model.Post;
import com.example.blog_back_app.post.repository.PostRepository;
import com.example.blog_back_app.post.service.PostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class PostServiceImplTest {
    @MockitoSpyBean
    private final PostRepository postRepository;
    private final PostService postService;

    @Autowired
    public PostServiceImplTest(PostRepository postRepository, PostService postService) {
        this.postRepository = postRepository;
        this.postService = postService;
    }

    @BeforeEach
    public void beforeEach() {
        var firstMockPost = new Post(null,
                "First test post",
                "First test post description",
                0L,
                List.of("First tag", "Third tag"),
                List.of());
        var secondMockPost = new Post(null,
                "Second test post",
                "Second test post description",
                0L,
                List.of("Fourth tag", "Fifth tag"),
                List.of());

        postRepository.saveAll(List.of(firstMockPost, secondMockPost));
    }

    @AfterEach
    public void afterEach() {
        postRepository.deleteAll();
    }

    @Test
    public void getById_shouldReturnPostById() {
        var postsPagination = postService.getAll("", 0, 10);
        var post = postService.getById(postsPagination.posts().stream()
                .filter(p -> "First test post".equals(p.title())).toList().getFirst().id());

        assertNotNull(post);
        assertEquals("First test post", post.title());
        assertEquals("First test post description", post.text());
        assertEquals(0L, post.likesCount());
        assertEquals(List.of("First tag", "Third tag"), post.tags());

        var postIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(postRepository).findById(postIdArgumentCaptor.capture());

        assertEquals(post.id(), postIdArgumentCaptor.getValue());
    }

    @Test
    public void getById_shouldThrowNotFoundException() {
        var uuid = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> postService.getById(uuid));

        var postIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(postRepository).findById(postIdArgumentCaptor.capture());

        assertEquals(uuid, postIdArgumentCaptor.getValue());
    }

    @Test
    public void create_shouldCreatePost() {
        var postRequestDto = new PostRequestDto(
                "First test post",
                "First test post description",
                List.of("First tag"));
        var postResponseDto = postService.create(postRequestDto);

        assertNotNull(postResponseDto);
        assertEquals("First test post", postResponseDto.title());
        assertEquals("First test post description", postResponseDto.text());
        assertEquals(0L, postResponseDto.likesCount());
        assertEquals(List.of("First tag"), postResponseDto.tags());
        assertEquals(0L, postResponseDto.commentsCount());

        var postArgumentCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postArgumentCaptor.capture());
        var saved = postArgumentCaptor.getValue();

        assertEquals("First test post", saved.getTitle());
        assertEquals("First test post description", saved.getText());
        assertEquals(List.of("First tag"), saved.getTags());
    }

    @Test
    public void getAll_shouldReturnOnePost() {
        var paginationResponseDto = postService.getAll("First test post", 0, 10);

        assertFalse(paginationResponseDto.posts().isEmpty());
        assertEquals(1, paginationResponseDto.posts().size());
        assertEquals(false, paginationResponseDto.hasNext());
        assertEquals(1, paginationResponseDto.lastPage());

        var postArgumentCaptor = ArgumentCaptor.forClass(String.class);
        var pageRequestArgumentCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(postRepository).getAll(postArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());

        assertEquals("First test post", postArgumentCaptor.getValue());
        assertEquals(PageRequest.of(0, 10), pageRequestArgumentCaptor.getValue());
    }

    @Test
    public void getAll_shouldReturnTwoPosts() {
        var paginationResponseDto = postService.getAll("", 0, 10);

        assertFalse(paginationResponseDto.posts().isEmpty());
        assertEquals(2, paginationResponseDto.posts().size());
        assertEquals(1, paginationResponseDto.lastPage());

        var postArgumentCaptor = ArgumentCaptor.forClass(String.class);
        var pageRequestArgumentCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(postRepository).getAll(postArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());

        assertEquals("", postArgumentCaptor.getValue());
        assertEquals(PageRequest.of(0, 10), pageRequestArgumentCaptor.getValue());
    }

    @Test
    public void getAll_shouldReturnNoPosts() {
        var paginationResponseDto = postService.getAll("no data", 0, 10);

        assertTrue(paginationResponseDto.posts().isEmpty());
        assertEquals(0, paginationResponseDto.lastPage());
        assertEquals(false, paginationResponseDto.hasNext());
        assertEquals(false, paginationResponseDto.hasPrev());

        var postArgumentCaptor = ArgumentCaptor.forClass(String.class);
        var pageRequestArgumentCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(postRepository).getAll(postArgumentCaptor.capture(), pageRequestArgumentCaptor.capture());

        assertEquals("no data", postArgumentCaptor.getValue());
        assertEquals(PageRequest.of(0, 10), pageRequestArgumentCaptor.getValue());
    }

    @Test
    public void update_shouldUpdatePost() {
        var posts = postService.getAll("", 0, 10);
        var firstPost = posts.posts().getFirst();
        var responseDto = postService.update(firstPost.id(),
                new PostRequestDto("Updated title", "Updated text", List.of("Updated tag")));

        assertNotNull(responseDto);
        assertEquals("Updated title", responseDto.title());
        assertEquals("Updated text", responseDto.text());
        assertEquals(List.of("Updated tag"), responseDto.tags());

        var postArgumentCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postArgumentCaptor.capture());
        var saved = postArgumentCaptor.getValue();

        assertEquals("Updated title", saved.getTitle());
        assertEquals("Updated text", saved.getText());
        assertEquals(List.of("Updated tag"), saved.getTags());
    }

    @Test
    public void update_shouldThrowExceptionForNotExistingPost() {
        assertThrows(NotFoundException.class, () -> postService.update(UUID.randomUUID(),
                new PostRequestDto("First post title", "First post text", List.of("First tag"))));
    }

    @Test
    public void delete_shouldThrowExceptionForDeletedPost() {
        var posts = postService.getAll("", 0, 10);
        var firstPost = posts.posts().getFirst();
        postService.delete(firstPost.id());

        assertThrows(NotFoundException.class, () -> postService.getById(firstPost.id()));

        var postArgumentCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).delete(postArgumentCaptor.capture());
        var deleted = postArgumentCaptor.getValue();

        assertEquals(firstPost.text(), deleted.getText());
        assertEquals(firstPost.tags(), deleted.getTags());
        assertEquals(firstPost.title(), deleted.getTitle());
    }

    @Test
    public void delete_shouldThrowExceptionForNotExistingPost() {
        assertThrows(NotFoundException.class, () -> postService.getById(UUID.randomUUID()));
    }

    @Test
    public void incrementLike_shouldIncrementLikesByTwo() {
        var posts = postService.getAll("", 0, 10);
        var firstPost = posts.posts().getFirst();
        postService.incrementLike(firstPost.id());
        postService.incrementLike(firstPost.id());
        var responseDto = postService.getById(firstPost.id());

        assertEquals(2L, responseDto.likesCount());

        var postArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(postRepository, times(2)).incrementLike(postArgumentCaptor.capture());

        assertEquals(firstPost.id(), postArgumentCaptor.getAllValues().get(1));
        assertEquals(firstPost.id(), postArgumentCaptor.getAllValues().get(0));
    }

    @Test
    public void incrementLike_shouldThrowExceptionForNotFoundPost() {
        assertThrows(NotFoundException.class, () -> postService.incrementLike(UUID.randomUUID()));
    }
}