package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostPaginationResponseDto {

    private List<PostResponseDto> posts;

    private Boolean hasPrev;

    private Boolean hasNext;

    private Long lastPage;
}
