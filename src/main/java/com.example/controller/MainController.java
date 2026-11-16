package com.example.controller;

import com.example.dto.*;
import com.example.model.File;
import com.example.service.CommentService;
import com.example.service.FileService;
import com.example.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class MainController {
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
    public PostResponseDto createPost(@RequestBody PostRequestDto requestDto) {
        return postService.create(requestDto);
    }

    @PutMapping("/{postId}")
    public PostResponseDto updatePost(@PathVariable("postId") UUID postId, @RequestBody PostRequestDto requestDto) {
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
                                       @RequestParam("image") MultipartFile image) {
        try {
            fileService.save(postId, image);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping("/{postId}/image")
    public ResponseEntity<?> getImage(@PathVariable("postId") UUID postId) {
        File file;
        try {
            file = fileService.get(postId);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(file.getContent());
        } catch (Exception exception) {
            return ResponseEntity.unprocessableEntity().build();
        }
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
                                            @RequestBody CommentRequestDto comment) {
        return commentService.create(postId, comment);
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public CommentResponseDto updateComment(@PathVariable("postId") UUID postId,
                                            @PathVariable("commentId") UUID commentId,
                                            @RequestBody CommentRequestDto comment) {
        return commentService.update(postId, commentId, comment);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @ResponseStatus(NO_CONTENT)
    public void deleteComment(@PathVariable("postId") UUID postId, @PathVariable("commentId") UUID commentId) {
        commentService.delete(postId, commentId);
    }
}
