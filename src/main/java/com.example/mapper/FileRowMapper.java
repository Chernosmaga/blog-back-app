package com.example.mapper;

import com.example.model.File;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FileRowMapper implements RowMapper<File> {
    @Override
    public File mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new File(UUID.fromString(rs.getString("id")),
                rs.getString("filename"),
                rs.getBytes("content"),
                UUID.fromString(rs.getString("post_id")));
    }
}
