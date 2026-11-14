package com.example.configuration;

import com.example.mapper.CommentMapper;
import com.example.mapper.PostMapper;
import com.example.repository.*;
import com.example.service.*;
import com.example.utilities.UtilService;
import org.mapstruct.factory.Mappers;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

@Configuration
@ActiveProfiles("test")
@PropertySource("classpath:application.properties")
public class TestConfiguration {

    @Bean
    public DataSource dataSource(@Value("${spring.datasource.url}") String url,
                                 @Value("${spring.datasource.username}") String username,
                                 @Value("${spring.datasource.password}") String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public PostService postService(PostRepository postRepository,
                                   CommentRepository commentRepository,
                                   PostMapper postMapper) {
        return new PostServiceImpl(postRepository, commentRepository, postMapper);
    }

    @Bean
    public CommentService commentService(CommentRepository commentRepository,
                                         UtilService utilService,
                                         CommentMapper commentMapper) {
        return new CommentServiceImpl(commentRepository, utilService, commentMapper);
    }

    @Bean
    public PostRepository postRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcPostRepository(jdbcTemplate);
    }

    @Bean
    public CommentRepository commentRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcCommentRepository(jdbcTemplate);
    }

    @Bean
    public PostMapper postMapper() {
        return Mappers.getMapper(PostMapper.class);
    }

    @Bean
    public CommentMapper commentMapper() {
        return Mappers.getMapper(CommentMapper.class);
    }

    @Bean
    public UtilService utilService(PostRepository postRepository) {
        return new UtilService(postRepository);
    }

    @Bean
    public FileRepository fileRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcFileRepository(jdbcTemplate);
    }

    @Bean
    public FileServiceImpl fileService(FileRepository fileRepository, UtilService utilService) {
        return new FileServiceImpl(fileRepository, utilService);
    }
}
