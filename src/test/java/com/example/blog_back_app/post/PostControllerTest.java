package com.example.blog_back_app.post;

import com.example.blog_back_app.comment.dto.CommentRequestDto;
import com.example.blog_back_app.comment.dto.CommentResponseDto;
import com.example.blog_back_app.comment.service.CommentService;
import com.example.blog_back_app.exception.NotFoundException;
import com.example.blog_back_app.exception.ValidationException;
import com.example.blog_back_app.exception.controller.ExceptionController;
import com.example.blog_back_app.file.model.File;
import com.example.blog_back_app.file.service.FileService;
import com.example.blog_back_app.post.controller.PostController;
import com.example.blog_back_app.post.dto.LikesCountResponseDto;
import com.example.blog_back_app.post.dto.PostPaginationResponseDto;
import com.example.blog_back_app.post.dto.PostRequestDto;
import com.example.blog_back_app.post.dto.PostResponseDto;
import com.example.blog_back_app.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@Import(ExceptionController.class)
public class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PostService postService;
    @MockitoBean
    private FileService fileService;
    @MockitoBean
    private CommentService commentService;

    private final UUID POST_MOCK_UUID = UUID.randomUUID();
    private final UUID COMMENT_MOCK_UUID = UUID.randomUUID();

    @BeforeEach
    public void beforeEach() {
        var posts = List.of(
                new PostResponseDto(
                        UUID.randomUUID(),
                        "First post",
                        "First post text",
                        0L,
                        0L,
                        List.of("First tag")),
                new PostResponseDto(
                        UUID.randomUUID(),
                        "Second post",
                        "Second post text",
                        1L,
                        2L,
                        List.of("Second tag")));
        var comments = List.of(
                new CommentResponseDto(UUID.randomUUID(), POST_MOCK_UUID, "First comment"),
                new CommentResponseDto(UUID.randomUUID(), POST_MOCK_UUID, "Second comment"));

        when(postService.getAll("First post", 0, 10))
                .thenReturn(new PostPaginationResponseDto(List.of(posts.getFirst()),
                        false, false, 0L));
        when(postService.getAll("", 0, 10))
                .thenReturn(new PostPaginationResponseDto(posts, false, false, 0L));
        when(postService.getAll("no data", 0, 10))
                .thenReturn(new PostPaginationResponseDto(List.of(), false, false, 0L));
        when(commentService.getAllByPostId(POST_MOCK_UUID)).thenReturn(comments);
    }

    @Test
    public void getAllPosts_shouldReturnOnePost() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "First post")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.posts[*]", hasSize(1)))
                .andExpect(jsonPath("$.lastPage").value(0));
    }

    @Test
    public void getAllPosts_shouldReturnTwoPosts() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.posts[*]", hasSize(2)))
                .andExpect(jsonPath("$.lastPage").value(0));
    }

    @Test
    public void getAllPosts_shouldReturnNoPosts() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "no data")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.posts[*]", hasSize(0)))
                .andExpect(jsonPath("$.lastPage").value(0));
    }

    @Test
    public void getPostById_shouldReturnPostById() throws Exception {
        when(postService.getById(POST_MOCK_UUID)).thenReturn(new PostResponseDto(
                UUID.randomUUID(),
                "First post",
                "First post text",
                0L,
                0L,
                List.of("First tag")));

        mockMvc.perform(get("/api/posts/{postId}", POST_MOCK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("First post"))
                .andExpect(jsonPath("$.text").value("First post text"));
    }

    @Test
    public void getPostById_shouldThrowExceptionIfPostNotFound() throws Exception {
        when(postService.getById(POST_MOCK_UUID)).thenThrow(new NotFoundException("Requested comment was not found"));

        mockMvc.perform(get("/api/posts/{postId}", POST_MOCK_UUID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createPost_shouldCreatePost() throws Exception {
        var requestDto = new PostRequestDto("Second post", "Second post title", List.of("Second tag"));
        var request = "{\"title\":\"Second post\",\"text\":\"Second post title\",\"tags\":[\"Second tag\"]}";
        when(postService.create(requestDto))
                .thenReturn(new PostResponseDto(UUID.randomUUID(),
                        "Second post",
                        "Second post title",
                        0L,
                        0L,
                        List.of("Second tag")));

        mockMvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Second post"))
                .andExpect(jsonPath("$.text").value("Second post title"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    public void createPost_shouldThrowExceptionIfTitleIsEmpty() throws Exception {
        var requestDto = new PostRequestDto("", "Second post title", List.of("Second tag"));
        var request = "{\"title\":\"\",\"text\":\"Second post title\",\"tags\":[\"Second tag\"]}";
        when(postService.create(requestDto))
                .thenThrow(new ValidationException("Title is required but wasn't provided"));

        mockMvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Title is required but wasn't provided"));
    }

    @Test
    public void createPost_shouldThrowExceptionIfTextIsEmpty() throws Exception {
        var requestDto = new PostRequestDto("Second post", "", List.of("Second tag"));
        var request = "{\"title\":\"Second title\",\"text\":\"\",\"tags\":[\"Second tag\"]}";
        when(postService.create(requestDto))
                .thenThrow(new ValidationException("Text is required but wasn't provided"));

        mockMvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Text field is required but not present"));
    }

    @Test
    public void createPost_shouldThrowExceptionIfTagsAreEmpty() throws Exception {
        var requestDto = new PostRequestDto("Second post", "Second post title", List.of());
        var request = "{\"title\":\"Second title\",\"text\":\"Second post text\",\"tags\":[]}";
        when(postService.create(requestDto))
                .thenThrow(new ValidationException("Tags are required but not provided"));

        mockMvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tags are required but not provided"));

    }

    @Test
    public void updatePost_shouldUpdatePostById() throws Exception {
        var requestDto = new PostRequestDto("Updated title", "Updated post text", List.of("Updated tag"));
        var request = "{\"title\":\"Updated title\",\"text\":\"Updated post text\",\"tags\":[\"Updated tag\"]}";
        when(postService.update(POST_MOCK_UUID, requestDto))
                .thenReturn(new PostResponseDto(POST_MOCK_UUID,
                        "Updated title",
                        "Updated post text",
                        0L,
                        0L,
                        List.of("Updated tag")));

        mockMvc.perform(put("/api/posts/{postId}", POST_MOCK_UUID)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    public void updatePost_shouldThrowExceptionIfTitleIsEmpty() throws Exception {
        var requestDto = new PostRequestDto("", "Updated post text", List.of("Updated tag"));
        var request = "{\"title\":\"\",\"text\":\"Updated post text\",\"tags\":[\"Updated tag\"]}";
        when(postService.update(POST_MOCK_UUID, requestDto))
                .thenThrow(new ValidationException("Title is required but wasn't provided"));

        mockMvc.perform(put("/api/posts/{postId}", POST_MOCK_UUID)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Title is required but wasn't provided"));

    }

    @Test
    public void updatePost_shouldThrowExceptionIfTextIsEmpty() throws Exception {
        var requestDto = new PostRequestDto("Updated title", "Updated post text", List.of("Updated tag"));
        var request = "{\"title\":\"Updated title\",\"text\":\"\",\"tags\":[\"Updated tag\"]}";
        when(postService.update(POST_MOCK_UUID, requestDto))
                .thenThrow(new ValidationException("Text is required but wasn't provided"));

        mockMvc.perform(put("/api/posts/{postId}", POST_MOCK_UUID)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Text field is required but not present"));
    }

    @Test
    public void updatePost_shouldThrowExceptionIfTagsAreEmpty() throws Exception {
        var requestDto = new PostRequestDto("Updated title", "Updated post text", List.of());
        var request = "{\"title\":\"Updated title\",\"text\":\"Updated post text\",\"tags\":[]}";
        when(postService.update(POST_MOCK_UUID, requestDto))
                .thenThrow(new ValidationException("Tags are required but not provided"));

        mockMvc.perform(put("/api/posts/{postId}", POST_MOCK_UUID)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tags are required but not provided"));
    }

    @Test
    public void updatePost_shouldThrowExceptionIfPostWasNotFound() throws Exception {
        var request = "{\"title\":\"Second title\",\"text\":\"Second post text\",\"tags\":[\"Second tag\"]}";
        when(postService.update(POST_MOCK_UUID,
                new PostRequestDto("Second title", "Second post text", List.of("Second tag"))))
                .thenThrow(new NotFoundException("Requested post wasn't found"));

        mockMvc.perform(put("/api/posts/{postId}", POST_MOCK_UUID)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post wasn't found"));
    }

    @Test
    public void deletePost_shouldDeletePost() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}", POST_MOCK_UUID))
                .andExpect(status().isNoContent());

        verify(postService, times(1)).delete(POST_MOCK_UUID);
    }

    @Test
    public void deletePost_shouldThrowExceptionIfPostWasNotFound() throws Exception {
        doThrow(new NotFoundException("Requested post was not found")).when(postService).delete(POST_MOCK_UUID);

        mockMvc.perform(delete("/api/posts/{postId}", POST_MOCK_UUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void incrementLike_shouldIncrementLike() throws Exception {
        when(postService.incrementLike(POST_MOCK_UUID)).thenReturn(new LikesCountResponseDto(1L));

        mockMvc.perform(post("/api/posts/{postId}/likes", POST_MOCK_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesCount").value(1));
    }

    @Test
    public void incrementLike_shouldThrowExceptionIfPostWasNotFound() throws Exception {
        when(postService.incrementLike(POST_MOCK_UUID))
                .thenThrow(new NotFoundException("Requested post was not found"));

        mockMvc.perform(post("/api/posts/{postId}/likes", POST_MOCK_UUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void save_shouldSaveFile() throws Exception {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("image",
                "test.jpg", "image/jpg", new byte[]{0, 1, 2, 3});

        mockMvc.perform(multipart("/api/posts/{postId}/image", POST_MOCK_UUID)
                        .file(mockMultipartFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());

        verify(fileService, times(1)).save(POST_MOCK_UUID, mockMultipartFile);
    }

    @Test
    public void save_shouldThrowExceptionIfPostNotFound() throws Exception {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("image",
                "test.jpg", "image/jpg", new byte[]{0, 1, 2, 3});
        doThrow(new NotFoundException("Requested post was not found"))
                .when(fileService).save(POST_MOCK_UUID, mockMultipartFile);

        mockMvc.perform(multipart("/api/posts/{postId}/image", POST_MOCK_UUID)
                        .file(mockMultipartFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isNotFound());
    }

    @Test
    public void save_shouldThrowExceptionIfFileIsNull() throws Exception {
        doThrow(new ValidationException("File content is empty")).when(fileService).save(POST_MOCK_UUID, null);

        mockMvc.perform(multipart("/api/posts/{postId}/image", POST_MOCK_UUID)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void get_shouldReturnImage() throws Exception {
        when(fileService.get(POST_MOCK_UUID))
                .thenReturn(new File(UUID.randomUUID(), "test.jpeg", new byte[]{0, 1, 2, 3}, null));

        mockMvc.perform(get("/api/posts/{postId}/image", POST_MOCK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(new byte[]{0, 1, 2, 3}));
    }

    @Test
    public void get_shouldThrowExceptionIfFileNotFound() throws Exception {
        doThrow(new NotFoundException("File wasn't found")).when(fileService).get(POST_MOCK_UUID);

        mockMvc.perform(get("/api/posts/{postId}/image", POST_MOCK_UUID)).andExpect(status().isNotFound());
    }

    @Test
    public void getAllByPostId_shouldReturnTwoComments() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments", POST_MOCK_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*]", hasSize(2)));
    }

    @Test
    public void getAllByPostId_shouldThrowExceptionIfPostNotExists() throws Exception {
        when(commentService.getAllByPostId(COMMENT_MOCK_UUID))
                .thenThrow(new NotFoundException("Requested post was not found"));

        mockMvc.perform(get("/api/posts/{postId}/comments", COMMENT_MOCK_UUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void getAllByPostId_shouldReturnZeroComments() throws Exception {
        when(commentService.getAllByPostId(COMMENT_MOCK_UUID)).thenReturn(List.of());

        mockMvc.perform(get("/api/posts/{postId}/comments", COMMENT_MOCK_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*]", empty()));
    }

    @Test
    public void getByIdAndPostId_shouldReturnComment() throws Exception {
        when(commentService.getByIdAndPostId(COMMENT_MOCK_UUID, POST_MOCK_UUID))
                .thenReturn(new CommentResponseDto(UUID.randomUUID(), POST_MOCK_UUID, "First comment text"));

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", POST_MOCK_UUID, COMMENT_MOCK_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text").value("First comment text"));
    }

    @Test
    public void getByIdAndPostId_shouldThrowExceptionIfCommentWasNotFound() throws Exception {
        when(commentService.getByIdAndPostId(COMMENT_MOCK_UUID, POST_MOCK_UUID))
                .thenThrow(new NotFoundException("Comment was not found"));

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", POST_MOCK_UUID, COMMENT_MOCK_UUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Comment was not found"));
    }

    @Test
    public void getByIdAndPostId_shouldThrowExceptionIfPostWasNotFound() throws Exception {
        when(commentService.getByIdAndPostId(COMMENT_MOCK_UUID, POST_MOCK_UUID))
                .thenThrow(new NotFoundException("Requested post was not found"));

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", POST_MOCK_UUID, COMMENT_MOCK_UUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void createComment_shouldCreateAndReturnComment() throws Exception {
        var requestDto = new CommentRequestDto(POST_MOCK_UUID, "Test comment");
        var request = "{\"postId\":\"" + POST_MOCK_UUID + "\",\"text\":\"Test comment\"}";
        when(commentService.create(POST_MOCK_UUID, requestDto))
                .thenReturn(new CommentResponseDto(COMMENT_MOCK_UUID, POST_MOCK_UUID, "Test comment"));

        mockMvc.perform(post("/api/posts/{postId}/comments", POST_MOCK_UUID)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text").value("Test comment"));
    }

    @Test
    public void createComment_shouldThrowExceptionIfPostNotFound() throws Exception {
        var requestDto = new CommentRequestDto(POST_MOCK_UUID, "Test comment");
        var request = "{\"postId\":\"" + POST_MOCK_UUID + "\",\"text\":\"Test comment\"}";
        when(commentService.create(POST_MOCK_UUID, requestDto))
                .thenThrow(new NotFoundException("Requested post was not found"));

        mockMvc.perform(post("/api/posts/{postId}/comments", POST_MOCK_UUID)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void createComment_shouldThrowExceptionIfPostIdIsNull() throws Exception {
        var requestDto = new CommentRequestDto(POST_MOCK_UUID, "Test comment");
        var request = "{\"postId\":null,\"text\":\"Test comment\"}";
        when(commentService.create(POST_MOCK_UUID, requestDto)).thenThrow(new NotFoundException("Post id is required"));

        mockMvc.perform(post("/api/posts/{postId}/comments", POST_MOCK_UUID)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Post id is required"));
    }

    @Test
    public void createComment_shouldThrowExceptionIfTextIsEmpty() throws Exception {
        var requestDto = new CommentRequestDto(POST_MOCK_UUID, "");
        var request = "{\"postId\":\"" + POST_MOCK_UUID + "\",\"text\":\"\"}";
        when(commentService.create(POST_MOCK_UUID, requestDto))
                .thenThrow(new ValidationException("Required text field is not present"));

        mockMvc.perform(post("/api/posts/{postId}/comments", POST_MOCK_UUID)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required text field is not present"));
    }

    @Test
    public void updateComment_shouldUpdateComment() throws Exception {
        var requestDto = new CommentRequestDto(POST_MOCK_UUID, "Updated comment");
        var request = "{\"postId\":\"" + POST_MOCK_UUID + "\",\"text\":\"Updated comment\"}";
        when(commentService.update(POST_MOCK_UUID, COMMENT_MOCK_UUID, requestDto))
                .thenReturn(new CommentResponseDto(COMMENT_MOCK_UUID, POST_MOCK_UUID, "Updated comment"));

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", POST_MOCK_UUID, COMMENT_MOCK_UUID)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated comment"));
    }

    @Test
    public void updateComment_shouldThrowExceptionIfPostIsNull() throws Exception {
        var requestDto = new CommentRequestDto(null, "Updated comment");
        var request = "{\"postId\":" + null + ",\"text\":\"Updated comment\"}";
        when(commentService.update(POST_MOCK_UUID, COMMENT_MOCK_UUID, requestDto))
                .thenThrow(new ValidationException("Post id is required"));

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", POST_MOCK_UUID, COMMENT_MOCK_UUID)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Post id is required"));
    }

    @Test
    public void updateComment_shouldThrowExceptionIfTextIsEmpty() throws Exception {
        var requestDto = new CommentRequestDto(POST_MOCK_UUID, "");
        var request = "{\"postId\":\"" + POST_MOCK_UUID + "\",\"text\":\"\"}";
        when(commentService.update(POST_MOCK_UUID, COMMENT_MOCK_UUID, requestDto))
                .thenThrow(new ValidationException("Required text field is not present"));

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", POST_MOCK_UUID, COMMENT_MOCK_UUID)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required text field is not present"));
    }

    @Test
    public void updateComment_shouldThrowExceptionIfPostWasNotFound() throws Exception {
        var requestDto = new CommentRequestDto(POST_MOCK_UUID, "Updated comment");
        var request = "{\"postId\":\"" + POST_MOCK_UUID + "\",\"text\":\"Updated comment\"}";
        when(commentService.update(POST_MOCK_UUID, COMMENT_MOCK_UUID, requestDto))
                .thenThrow(new NotFoundException("Requested post was not found"));

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", POST_MOCK_UUID, COMMENT_MOCK_UUID)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void deleteComment_shouldDeleteComment() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", POST_MOCK_UUID, COMMENT_MOCK_UUID))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).delete(POST_MOCK_UUID, COMMENT_MOCK_UUID);
    }

    @Test
    public void deleteComment_shouldThrowExceptionIfPostWasNotFound() throws Exception {
        doThrow(new NotFoundException("Requested post was not found"))
                .when(commentService).delete(POST_MOCK_UUID, COMMENT_MOCK_UUID);

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", POST_MOCK_UUID, COMMENT_MOCK_UUID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void deleteComment_shouldThrowExceptionIfCommentWasNotFound() throws Exception {
        doThrow(new NotFoundException("Requested comment was not found"))
                .when(commentService).delete(POST_MOCK_UUID, COMMENT_MOCK_UUID);

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", POST_MOCK_UUID, COMMENT_MOCK_UUID))
                .andExpect(status().isNotFound());
    }
}
