package com.example.service;

import com.example.dto.CommentRequestDto;
import com.example.dto.CommentResponseDto;
import com.example.exception.DataViolationException;
import com.example.exception.NotFoundException;
import com.example.mapper.CommentMapper;
import com.example.model.Comment;
import com.example.repository.CommentRepository;
import com.example.utilities.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UtilService utilService;
    private final CommentMapper commentMapper;

    @Override
    public List<CommentResponseDto> getAllByPostId(UUID postId) {
        utilService.postExists(postId);
        return commentRepository.getAllByPostId(postId).stream()
                .map(comment -> toReturn(postId, comment)).toList();
    }

    @Override
    public CommentResponseDto getByIdAndPostId(UUID commentId, UUID postId) {
        Comment comment;
        try {
            comment = commentRepository.getByIdAndPostId(commentId, postId);
        } catch (Exception exception) {
            throw new NotFoundException("Comment was not found");
        }
        return toReturn(postId, comment);
    }

    @Override
    @Transactional
    public CommentResponseDto create(UUID postId, CommentRequestDto comment) {
        utilService.postExists(postId);
        comment.validate();

        UUID uuid;
        try {
            uuid = commentRepository.create(postId, commentMapper.toComment(comment));
        } catch (Exception exception) {
            throw new DataViolationException("Unable to add comment");
        }
        return toReturn(postId, commentRepository.getByIdAndPostId(uuid, postId));
    }

    @Override
    @Transactional
    public CommentResponseDto update(UUID postId, UUID commentId, CommentRequestDto comment) {
        utilService.postExists(postId);
        comment.validate();

        try {
            commentRepository.getByIdAndPostId(commentId, postId);
            commentRepository.update(postId, commentId, comment.getText());
        } catch (Exception exception) {
            throw new DataViolationException("Unable to update comment");
        }
        return toReturn(postId, commentRepository.getByIdAndPostId(commentId, postId));
    }

    @Override
    @Transactional
    public void delete(UUID postId, UUID commentId) {
        try {
            commentRepository.getByIdAndPostId(commentId, postId);
            commentRepository.delete(postId, commentId);
        } catch (Exception exception) {
            throw new NotFoundException("Requested comment was not found");
        }
    }

    private CommentResponseDto toReturn(UUID postId, Comment comment) {
        return new CommentResponseDto(comment.getId(), postId, comment.getText());
    }

}
