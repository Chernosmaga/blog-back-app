package com.example.dto;

import com.example.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CommentRequestDto {

    private UUID postId;

    private String text;

    public void validate() {
        if (postId == null) throw new ValidationException("Post id is required");
        if (text == null || text.isEmpty()) throw new ValidationException("Required text field is not present");
    }
}
