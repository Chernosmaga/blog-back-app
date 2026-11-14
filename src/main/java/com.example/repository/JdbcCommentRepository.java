package com.example.repository;

import com.example.mapper.CommentRowMapper;
import com.example.model.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JdbcCommentRepository implements CommentRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Comment> getAllByPostId(UUID postId) {
        return jdbcTemplate.query("select c.id, c.text from comments c where c.post_id = ?",
                new CommentRowMapper(), postId);
    }

    @Override
    public Comment getByIdAndPostId(UUID commentId, UUID postId) {
        return jdbcTemplate.queryForObject("select c.id, c.text from comments c " +
                "where c.id = ? and c.post_id = ?", new CommentRowMapper(), commentId, postId);
    }

    @Override
    public UUID create(UUID postId, Comment comment) {
        return jdbcTemplate.queryForObject("insert into comments (id, text, post_id) " +
                "values (gen_random_uuid(), ?, ?) returning id", UUID.class, comment.getText(), postId);
    }

    @Override
    public void update(UUID postId, UUID commentId, String text) {
        jdbcTemplate.update("update comments c set text = ? where c.id = ? and c.post_id = ?",
                text, commentId, postId);
    }

    @Override
    public void delete(UUID postId, UUID commentId) {
        jdbcTemplate.update("delete from comments c where c.id = ? and c.post_id = ?", commentId, postId);
    }

    @Override
    public long countForPost(UUID postId) {
        Long count = jdbcTemplate.queryForObject("select count(distinct c.id) from comments c where c.post_id = ?",
                Long.class, postId);
        return count != null ? count : 0;
    }
}
