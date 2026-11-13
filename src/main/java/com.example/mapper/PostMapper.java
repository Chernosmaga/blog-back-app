package com.example.mapper;

import com.example.dto.PostRequestDto;
import com.example.dto.PostResponseDto;
import com.example.model.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostResponseDto toPostResponseDto(Post post);

    Post toPost(PostRequestDto requestDto);
}
