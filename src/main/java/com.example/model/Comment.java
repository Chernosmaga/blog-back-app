package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Comment {

    private UUID id;

    private String text;
}
