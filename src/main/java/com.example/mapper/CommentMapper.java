package com.example.mapper;

import com.example.dto.CommentRequestDto;
import com.example.model.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toComment(CommentRequestDto requestDto);
}
