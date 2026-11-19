package com.example.repository;

import com.example.mapper.FileRowMapper;
import com.example.model.File;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JdbcFileRepository implements FileRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(UUID postId, String filename, byte[] content) {
        jdbcTemplate.update("insert into files (id, filename, content, post_id) " +
                "values (gen_random_uuid(), ?, ?, ?)", filename, content, postId);
    }

    @Override
    public File get(UUID postId) {
        return jdbcTemplate.queryForObject("select * from files where post_id = ?", new FileRowMapper(), postId);
    }
}
