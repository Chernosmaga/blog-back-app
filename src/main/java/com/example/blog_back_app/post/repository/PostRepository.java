package com.example.blog_back_app.post.repository;

import com.example.blog_back_app.post.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    @Query("select distinct p from Post p join p.tags pt " +
            "where p.title like concat('%', :search, '%') or p.text like concat('%', :search, '%') " +
            "or pt like concat('%', :search, '%')")
    Page<Post> getAll(String search, Pageable pageable);

    @Modifying
    @Query("update Post set likesCount = likesCount + 1 where id = :postId")
    void incrementLike(UUID postId);

    @Query("select p.likesCount from Post p where p.id = :postId")
    Long getLikesCountById(UUID postId);
}
