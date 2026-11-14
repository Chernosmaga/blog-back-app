package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class File {

    private UUID id;

    private String filename;

    private byte[] content;

    private UUID postId;
}
