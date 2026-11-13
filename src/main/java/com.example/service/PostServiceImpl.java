package com.example.service;

import com.example.dto.LikesCountResponseDto;
import com.example.dto.PostPaginationResponseDto;
import com.example.dto.PostRequestDto;
import com.example.dto.PostResponseDto;
import com.example.exception.DataViolationException;
import com.example.exception.NotFoundException;
import com.example.mapper.PostMapper;
import com.example.model.Post;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostMapper postMapper;

    @Override
    public PostPaginationResponseDto getAll(String search, int pageNumber, int pageSize) {
        PostPaginationResponseDto responseDto = new PostPaginationResponseDto();
        List<Post> posts = postRepository.getAll(search, pageNumber, pageSize);

        long totalCount = postRepository.countTotalPages(search);
        long totalPages = (totalCount + pageSize - 1) / pageSize;
        long lastPage = (totalPages / pageSize);

        responseDto.setPosts(posts.stream().map(postMapper::toPostResponseDto).toList());
        responseDto.setHasPrev(lastPage > 0);
        responseDto.setHasNext(pageSize < totalPages - 1);
        responseDto.setLastPage(lastPage);
        return responseDto;
    }

    @Override
    public PostResponseDto getById(UUID postId) {
        Post post;
        try {
            post = postRepository.getById(postId);
        } catch (Exception exception) {
            throw new NotFoundException("Required post was not found");
        }
        return toReturn(post);
    }

    @Override
    @Transactional
    public PostResponseDto create(PostRequestDto requestDto) {
        requestDto.validate();
        UUID uuid;
        try {
            uuid = postRepository.create(postMapper.toPost(requestDto));
            postRepository.addTags(uuid, requestDto.getTags());
        } catch (Exception exception) {
            throw new DataViolationException("Unable to create post");
        }
        return toReturn(postRepository.getById(uuid));
    }

    @Override
    @Transactional
    public PostResponseDto update(UUID postId, PostRequestDto requestDto) {
        requestDto.validate();
        Post post;
        try {
            postRepository.update(postId, requestDto.getTitle(), requestDto.getText());
            postRepository.deleteTags(postId);
            postRepository.addTags(postId, requestDto.getTags());
            post = postRepository.getById(postId);
        } catch (Exception exception) {
            throw new NotFoundException("Required post was not found");
        }
        return toReturn(post);
    }

    @Override
    @Transactional
    public void delete(UUID postId) {
        try {
            postRepository.delete(postId);
        } catch (Exception exception) {
            throw new NotFoundException("Post was not found");
        }
    }

    @Override
    @Transactional
    public LikesCountResponseDto incrementLike(UUID postId) {
        long likes;
        try {
            likes = postRepository.incrementLike(postId);
        } catch (Exception exception) {
            throw new NotFoundException("Required post was not found");
        }
        return new LikesCountResponseDto(likes);
    }

    private PostResponseDto toReturn(Post post) {
        PostResponseDto response = postMapper.toPostResponseDto(post);
        response.setCommentsCount(commentRepository.countForPost(post.getId()));
        return response;
    }
}
