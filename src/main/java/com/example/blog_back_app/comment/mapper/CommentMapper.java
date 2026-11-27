package com.example.blog_back_app.comment.mapper;

import com.example.blog_back_app.comment.dto.CommentRequestDto;
import com.example.blog_back_app.comment.model.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toComment(CommentRequestDto requestDto);
}