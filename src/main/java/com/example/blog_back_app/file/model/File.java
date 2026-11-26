package com.example.blog_back_app.file.model;

import com.example.blog_back_app.post.model.Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Table(name = "files")
@AllArgsConstructor
@NoArgsConstructor
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String filename;

    private byte[] content;

    @OneToOne
    @JoinColumn(name = "post_id")
    private Post post;
}
