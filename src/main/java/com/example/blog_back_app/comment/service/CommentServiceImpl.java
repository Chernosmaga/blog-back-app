package com.example.blog_back_app.comment.service;

import com.example.blog_back_app.comment.dto.CommentRequestDto;
import com.example.blog_back_app.comment.dto.CommentResponseDto;
import com.example.blog_back_app.comment.mapper.CommentMapper;
import com.example.blog_back_app.comment.model.Comment;
import com.example.blog_back_app.comment.repository.CommentRepository;
import com.example.blog_back_app.exception.NotFoundException;
import com.example.blog_back_app.utilities.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UtilService utilService;
    private final CommentMapper commentMapper;

    @Override
    public List<CommentResponseDto> getAllByPostId(UUID postId) {
        utilService.postExists(postId);
        return commentRepository.findAllByPostId(postId).stream()
                .map(comment -> toReturn(postId, comment)).toList();
    }

    @Override
    public CommentResponseDto getByIdAndPostId(UUID commentId, UUID postId) {
        utilService.postExists(postId);
        Comment found = returnByIdAndPostId(commentId, postId);
        return toReturn(postId, found);
    }

    @Override
    @Transactional
    public CommentResponseDto create(UUID postId, CommentRequestDto comment) {
        utilService.postExists(postId);
        utilService.postIdsAreEquals(postId, comment.postId());

        Comment toSave = commentMapper.toComment(comment);
        toSave.setPost(utilService.findById(postId));
        return toReturn(postId, commentRepository.save(toSave));
    }

    @Override
    @Transactional
    public CommentResponseDto update(UUID postId, UUID commentId, CommentRequestDto comment) {
        utilService.postExists(postId);
        utilService.postIdsAreEquals(postId, comment.postId());

        Comment found = returnByIdAndPostId(commentId, postId);
        found.setText(comment.text());
        return toReturn(postId, commentRepository.save(found));
    }

    @Override
    @Transactional
    public void delete(UUID postId, UUID commentId) {
        utilService.postExists(postId);
        commentRepository.delete(returnByIdAndPostId(commentId, postId));
    }

    private Comment returnByIdAndPostId(UUID commentId, UUID postId) {
        return commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new NotFoundException("Required comment was not found"));
    }

    private CommentResponseDto toReturn(UUID postId, Comment comment) {
        return new CommentResponseDto(comment.getId(), postId, comment.getText());
    }
}
