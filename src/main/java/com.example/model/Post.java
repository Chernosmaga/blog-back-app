package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    private UUID id;

    private String title;

    private String text;

    private Long likesCount;

    private List<String> tags;
}
