package com.example.mapper;

import com.example.model.Post;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PostRowMapper implements RowMapper<Post> {
    @Override
    public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
        List<String> tags = new ArrayList<>();
        String sqlArray = rs.getString("tags");
        if (sqlArray != null) tags = Arrays.stream(sqlArray.split(",")).toList();

        return new Post(
                UUID.fromString(rs.getString("id")),
                rs.getString("title"),
                rs.getString("text"),
                rs.getLong("likes_count"),
                tags);
    }
}
