package com.example.blog_back_app.comment.repository;

import com.example.blog_back_app.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findAllByPostId(UUID postId);

    @Query("select c from Comment c where c.id = :commentId and c.post.id = :postId")
    Optional<Comment> findByIdAndPostId(UUID commentId, UUID postId);
}
