package com.example.blog_back_app.post.mapper;

import com.example.blog_back_app.post.dto.PostRequestDto;
import com.example.blog_back_app.post.dto.PostResponseDto;
import com.example.blog_back_app.post.model.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostResponseDto toPostResponseDto(Post post);

    Post toPost(PostRequestDto requestDto);
}