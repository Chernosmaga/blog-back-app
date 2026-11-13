package com.example.dto;

import com.example.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PostRequestDto {

    private String title;

    private String text;

    private List<String> tags;

    public void validate() {
        if (title == null || title.isEmpty()) throw new ValidationException("Title is required but wasn't provided");
        if (text == null || text.isEmpty()) throw new ValidationException("Text field required but not present");
        if (tags.isEmpty()) throw new ValidationException("Tags are required but not provided");
    }
}
