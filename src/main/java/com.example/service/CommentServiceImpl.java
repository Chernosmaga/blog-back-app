package com.example.service;

import com.example.dto.CommentRequestDto;
import com.example.dto.CommentResponseDto;
import com.example.exception.NotFoundException;
import com.example.mapper.CommentMapper;
import com.example.model.Comment;
import com.example.repository.CommentRepository;
import com.example.utilities.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
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
        return commentRepository.getAllByPostId(postId).stream()
                .map(comment -> toReturn(postId, comment)).toList();
    }

    @Override
    public CommentResponseDto getByIdAndPostId(UUID commentId, UUID postId) {
        utilService.postExists(postId);

        Comment comment;
        try {
            comment = commentRepository.getByIdAndPostId(commentId, postId);
        } catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("Comment was not found");
        }
        return toReturn(postId, comment);
    }

    @Override
    @Transactional
    public CommentResponseDto create(UUID postId, CommentRequestDto comment) {
        utilService.postExists(postId);
        utilService.postIdsAreEquals(postId, comment.postId());

        UUID uuid = commentRepository.create(postId, commentMapper.toComment(comment));
        return toReturn(postId, commentRepository.getByIdAndPostId(uuid, postId));
    }

    @Override
    @Transactional
    public CommentResponseDto update(UUID postId, UUID commentId, CommentRequestDto comment) {
        utilService.postExists(postId);
        utilService.postIdsAreEquals(postId, comment.postId());
        utilService.commentExists(commentId);

        commentRepository.update(postId, commentId, comment.text());
        return toReturn(postId, commentRepository.getByIdAndPostId(commentId, postId));
    }

    @Override
    @Transactional
    public void delete(UUID postId, UUID commentId) {
        utilService.postExists(postId);
        utilService.commentExists(commentId);

        commentRepository.delete(postId, commentId);
    }

    private CommentResponseDto toReturn(UUID postId, Comment comment) {
        return new CommentResponseDto(comment.getId(), postId, comment.getText());
    }
}
