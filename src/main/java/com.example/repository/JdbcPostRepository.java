package com.example.repository;

import com.example.mapper.PostRowMapper;
import com.example.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JdbcPostRepository implements PostRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Post> getAll(String search, int pageNumber, int pageSize) {
        return jdbcTemplate.query("select p.id, p.title, p.text, p.likes_count, " +
                "string_agg(pt.tag, ',' order by pt.tag) as tags " +
                "from posts p join post_tags pt on p.id = pt.post_id " +
                "where p.title like concat('%', ?, '%') or p.text like concat('%', ?, '%') " +
                "or pt.tag like concat('%', ?, '%')" +
                "group by p.id, p.title, p.text, p.likes_count " +
                "offset ? limit ?", new PostRowMapper(), search, search, search, pageNumber, pageSize);
    }

    @Override
    public long countTotalPages(String search) {
        Long count = jdbcTemplate.queryForObject("select count(*) " +
                "from (select distinct(p.id) from posts p " +
                "join post_tags pt on p.id = pt.post_id " +
                "where p.title like concat('%', ?, '%') or p.text like concat('%', ?, '%') " +
                "or pt.tag like concat('%', ?, '%')) as total_pages", Long.class, search, search, search);
        return count != null ? count : 0L;
    }

    @Override
    public Post getById(UUID postId) {
        return jdbcTemplate.queryForObject("select p.id, p.title, p.text, p.likes_count, " +
                        "string_agg(pt.tag, ',' order by pt.tag) as tags " +
                        "from posts p " +
                        "join post_tags pt on p.id = pt.post_id " +
                        "where p.id = ? " +
                        "group by p.id, p.title, p.text, p.likes_count", new PostRowMapper(), postId);
    }

    @Override
    public UUID create(Post post) {
        return jdbcTemplate.queryForObject("insert into posts (id, title, text) " +
                "values (gen_random_uuid(), ?, ?) returning id", UUID.class, post.getTitle(), post.getText());
    }

    @Override
    public void update(UUID postId, String title, String text) {
        jdbcTemplate.update("update posts set title = ?, text = ? where id = ?", title, text, postId);
    }

    @Override
    public void delete(UUID postId) {
        jdbcTemplate.update("delete from posts p where p.id = ?", postId);
    }

    @Override
    public long incrementLike(UUID postId) {
        Long likesCount = jdbcTemplate.queryForObject("update posts set likes_count = likes_count + 1 " +
                "where id = ? returning likes_count", Long.class, postId);
        return likesCount != null ? likesCount : 0L;
    }

    @Override
    public void addTags(UUID postId, List<String> tags) {
        if (tags.isEmpty()) return;

        jdbcTemplate.batchUpdate("insert into post_tags (post_id, tag) values (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, postId);
                        ps.setObject(2, tags.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return tags.size();
                    }
        });
    }

    @Override
    public void deleteTags(UUID postId) {
        jdbcTemplate.update("delete from post_tags pt where pt.post_id = ?", postId);
    }
}
