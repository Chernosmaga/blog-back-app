package com.example.blog_back_app.post.controller;

import com.example.blog_back_app.comment.dto.CommentRequestDto;
import com.example.blog_back_app.comment.dto.CommentResponseDto;
import com.example.blog_back_app.comment.service.CommentService;
import com.example.blog_back_app.file.model.File;
import com.example.blog_back_app.file.service.FileService;
import com.example.blog_back_app.post.dto.LikesCountResponseDto;
import com.example.blog_back_app.post.dto.PostPaginationResponseDto;
import com.example.blog_back_app.post.dto.PostRequestDto;
import com.example.blog_back_app.post.dto.PostResponseDto;
import com.example.blog_back_app.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final CommentService commentService;
    private final FileService fileService;

    @GetMapping
    public PostPaginationResponseDto getAllPosts(@RequestParam(name = "search", required = false) String search,
                                                 @RequestParam(name = "pageNumber", defaultValue = "0") int pageNumber,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        return postService.getAll(search, pageNumber, pageSize);
    }

    @GetMapping("/{postId}")
    public PostResponseDto getPostById(@PathVariable("postId") UUID postId) {
        return postService.getById(postId);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public PostResponseDto createPost(@Validated @RequestBody PostRequestDto requestDto) {
        return postService.create(requestDto);
    }

    @PutMapping("/{postId}")
    public PostResponseDto updatePost(@PathVariable("postId") UUID postId,
                                      @Validated @RequestBody PostRequestDto requestDto) {
        return postService.update(postId, requestDto);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(NO_CONTENT)
    public void deletePost(@PathVariable("postId") UUID postId) {
        postService.delete(postId);
    }

    @PostMapping("/{postId}/likes")
    public LikesCountResponseDto incrementLikes(@PathVariable("postId") UUID postId) {
        return postService.incrementLike(postId);
    }

    @PutMapping("/{postId}/image")
    public ResponseEntity<?> saveImage(@PathVariable("postId") UUID postId,
                                       @RequestParam("image") MultipartFile image) throws IOException {
        fileService.save(postId, image);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}/image")
    public ResponseEntity<?> getImage(@PathVariable("postId") UUID postId) {
        File file = fileService.get(postId);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(file.getContent());
    }

    @GetMapping("/{postId}/comments")
    public List<CommentResponseDto> getAllByPostId(@PathVariable("postId") UUID postId) {
        return commentService.getAllByPostId(postId);
    }

    @GetMapping("/{postId}/comments/{commentId}")
    public CommentResponseDto getByIdAndPostId(@PathVariable("postId") UUID postId,
                                               @PathVariable("commentId") UUID commentId) {
        return commentService.getByIdAndPostId(commentId, postId);
    }

    @PostMapping("/{postId}/comments")
    @ResponseStatus(CREATED)
    public CommentResponseDto createComment(@PathVariable("postId") UUID postId,
                                            @Validated @RequestBody CommentRequestDto comment) {
        return commentService.create(postId, comment);
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public CommentResponseDto updateComment(@PathVariable("postId") UUID postId,
                                            @PathVariable("commentId") UUID commentId,
                                            @Validated @RequestBody CommentRequestDto comment) {
        return commentService.update(postId, commentId, comment);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @ResponseStatus(NO_CONTENT)
    public void deleteComment(@PathVariable("postId") UUID postId, @PathVariable("commentId") UUID commentId) {
        commentService.delete(postId, commentId);
    }
}