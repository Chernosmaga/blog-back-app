package example.configuration;

import com.example.mapper.CommentMapper;
import com.example.mapper.PostMapper;
import com.example.repository.*;
import com.example.service.*;
import com.example.utilities.UtilService;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@ComponentScan(
        basePackages = "com.example",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {
                org.springframework.stereotype.Service.class,
                org.springframework.stereotype.Repository.class,
                org.springframework.stereotype.Component.class
        })
)
@Import(DatabaseTestConfiguration.class)
public class RootTestConfiguration {

    @Bean
    @Profile("test")
    public PostService postService(PostRepository postRepository,
                                   UtilService utilService,
                                   CommentRepository commentRepository,
                                   PostMapper postMapper) {
        return new PostServiceImpl(postRepository, utilService, commentRepository, postMapper);
    }

    @Bean
    @Profile("test")
    public CommentService commentService(CommentRepository commentRepository,
                                         UtilService utilService,
                                         CommentMapper commentMapper) {
        return new CommentServiceImpl(commentRepository, utilService, commentMapper);
    }

    @Bean
    @Profile("test")
    public PostRepository postRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcPostRepository(jdbcTemplate);
    }

    @Bean
    @Profile("test")
    public CommentRepository commentRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcCommentRepository(jdbcTemplate);
    }

    @Bean
    @Profile("test")
    public PostMapper postMapper() {
        return Mappers.getMapper(PostMapper.class);
    }

    @Bean
    @Profile("test")
    public CommentMapper commentMapper() {
        return Mappers.getMapper(CommentMapper.class);
    }

    @Bean
    @Profile("test")
    public UtilService utilService(PostRepository postRepository, CommentRepository commentRepository) {
        return new UtilService(postRepository, commentRepository);
    }

    @Bean
    @Profile("test")
    public FileRepository fileRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcFileRepository(jdbcTemplate);
    }

    @Bean
    @Profile("test")
    public FileServiceImpl fileService(FileRepository fileRepository, UtilService utilService) {
        return new FileServiceImpl(fileRepository, utilService);
    }
}
