package com.example.blog_back_app.post.service;

import com.example.blog_back_app.comment.repository.CommentRepository;
import com.example.blog_back_app.file.repository.FileRepository;
import com.example.blog_back_app.post.dto.LikesCountResponseDto;
import com.example.blog_back_app.post.dto.PostPaginationResponseDto;
import com.example.blog_back_app.post.dto.PostRequestDto;
import com.example.blog_back_app.post.dto.PostResponseDto;
import com.example.blog_back_app.post.mapper.PostMapper;
import com.example.blog_back_app.post.model.Post;
import com.example.blog_back_app.post.repository.PostRepository;
import com.example.blog_back_app.utilities.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final FileRepository fileRepository;
    private final CommentRepository commentRepository;
    private final UtilService utilService;
    private final PostMapper postMapper;

    @Override
    public PostPaginationResponseDto getAll(String search, int pageNumber, int pageSize) {
        Page<Post> posts = postRepository.getAll(search, PageRequest.of(pageNumber, pageSize));

        long totalPages = posts.getTotalPages();
        long lastPage = (totalPages / pageSize);

        return new PostPaginationResponseDto(
                posts.stream().map(this::toReturn).toList(),
                lastPage > 0,
                pageSize < totalPages - 1,
                totalPages
        );
    }

    @Override
    public PostResponseDto getById(UUID postId) {
        return toReturn(utilService.findById(postId));
    }

    @Override
    @Transactional
    public PostResponseDto create(PostRequestDto post) {
        return toReturn(postRepository.save(postMapper.toPost(post)));
    }

    @Override
    @Transactional
    public PostResponseDto update(UUID postId, PostRequestDto post) {
        Post found = utilService.findById(postId);
        found.setText(post.text());
        found.setTitle(post.title());
        found.setTags(post.tags());
        return toReturn(postRepository.save(found));
    }

    @Override
    @Transactional
    public void delete(UUID postId) {
        Post post = utilService.findById(postId);
        fileRepository.deleteAllByPostId(postId);
        postRepository.delete(post);
    }

    @Override
    @Transactional
    public LikesCountResponseDto incrementLike(UUID postId) {
        utilService.postExists(postId);
        postRepository.incrementLike(postId);
        return new LikesCountResponseDto(postRepository.getLikesCountById(postId));
    }

    private PostResponseDto toReturn(Post post) {
        PostResponseDto response = postMapper.toPostResponseDto(post);
        return response.withCommentsCount(commentRepository.countAllByPostId(post.getId()));
    }
}
