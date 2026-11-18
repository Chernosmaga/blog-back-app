package example.controller;

import example.configuration.ControllerTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ActiveProfiles("test")
@SpringJUnitConfig(classes = ControllerTestConfiguration.class)
public class MainControllerTest {
    private final WebApplicationContext wac;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MainControllerTest(WebApplicationContext wac, JdbcTemplate jdbcTemplate) {
        this.wac = wac;
        this.jdbcTemplate = jdbcTemplate;
    }

    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();

        jdbcTemplate.update("delete from posts");
        jdbcTemplate.update("delete from files");
        jdbcTemplate.update("insert into posts (id, title, text, likes_count) " +
                "values (gen_random_uuid(), 'First post', 'First post title', 10)");
        jdbcTemplate.update("insert into posts (id, title, text, likes_count) " +
                "values (gen_random_uuid(), 'Second post', 'Second post title', 0)");
        var firstPostId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.text = 'First post title'", UUID.class);
        var secondPostId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.text = 'Second post title'", UUID.class);
        jdbcTemplate.update("insert into post_tags (post_id, tag) values (?, 'First tag')", firstPostId);
        jdbcTemplate.update("insert into post_tags (post_id, tag) values (?, 'Second tag')", secondPostId);
        jdbcTemplate.update("insert into comments (id, text, post_id) " +
                "values (gen_random_uuid(), 'First comment text', ?)", firstPostId);
        jdbcTemplate.update("insert into comments (id, text, post_id) " +
                "values (gen_random_uuid(), 'Second comment text', ?)", firstPostId);
    }

    @Test
    public void testAvailablePaths() {
        var mapping = wac.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : map.entrySet()) {
            System.out.println("path: " + entry.getKey().toString() + "\n" +
                    "method: " + entry.getValue().getMethod().getName());
        }
    }

    @Test
    public void getAllPosts_shouldReturnOnePost() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "First post")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.posts[*]", hasSize(1)))
                .andExpect(jsonPath("$.lastPage").value(0));
    }

    @Test
    public void getAllPosts_shouldReturnTwoPosts() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.posts[*]", hasSize(2)))
                .andExpect(jsonPath("$.lastPage").value(0));;
    }

    @Test
    public void getAllPosts_shouldReturnNoPosts() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "no data")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.posts[*]", hasSize(0)))
                .andExpect(jsonPath("$.lastPage").value(0));;
    }

    @Test
    public void getPostById_shouldReturnPostById() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.text = 'First post title'", UUID.class);

        mockMvc.perform(get("/api/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("First post"))
                .andExpect(jsonPath("$.text").value("First post title"));
    }

    @Test
    public void getPostById_shouldThrowExceptionIfPostNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void create_shouldCreatePost() throws Exception {
        var request = "{\"title\":\"Second post\",\"text\":\"Second post title\",\"tags\":[\"Second tag\"]}";

        mockMvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Second post"))
                .andExpect(jsonPath("$.text").value("Second post title"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    public void create_shouldThrowExceptionIfTitleIsEmpty() throws Exception {
        var request = "{\"title\":\"\",\"text\":\"Second post title\",\"tags\":[\"Second tag\"]}";

        mockMvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Title is required but wasn't provided"));
    }

    @Test
    public void create_shouldThrowExceptionIfTextIsEmpty() throws Exception {
        var request = "{\"title\":\"Second title\",\"text\":\"\",\"tags\":[\"Second tag\"]}";

        mockMvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Text field is required but not present"));

    }

    @Test
    public void create_shouldThrowExceptionIfTagsAreEmpty() throws Exception {
        var request = "{\"title\":\"Second title\",\"text\":\"Second post text\",\"tags\":[]}";

        mockMvc.perform(post("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Tags are required but not provided"));

    }

    @Test
    public void update_shouldUpdatePostById() throws Exception {
        var request = "{\"title\":\"Updated title\",\"text\":\"Updated post text\",\"tags\":[\"Updated tag\"]}";
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);

        mockMvc.perform(put("/api/posts/{postId}", postId)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    public void update_shouldThrowExceptionIfTitleIsEmpty() throws Exception {
        var request = "{\"title\":\"\",\"text\":\"Updated post text\",\"tags\":[\"Updated tag\"]}";
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);

        mockMvc.perform(put("/api/posts/{postId}", postId)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Title is required but wasn't provided"));

    }

    @Test
    public void update_shouldThrowExceptionIfTextIsEmpty() throws Exception {
        var request = "{\"title\":\"Updated title\",\"text\":\"\",\"tags\":[\"Updated tag\"]}";
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);

        mockMvc.perform(put("/api/posts/{postId}", postId)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Text field is required but not present"));

    }

    @Test
    public void update_shouldThrowExceptionIfTagsAreEmpty() throws Exception {
        var request = "{\"title\":\"Updated title\",\"text\":\"Updated post text\",\"tags\":[]}";
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);

        mockMvc.perform(put("/api/posts/{postId}", postId)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Tags are required but not provided"));
    }

    @Test
    public void update_shouldThrowExceptionIfPostWasNotFound() throws Exception {
        var request = "{\"title\":\"Second title\",\"text\":\"Second post text\",\"tags\":[\"Second tag\"]}";

        mockMvc.perform(put("/api/posts/{postId}", UUID.randomUUID())
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void delete_shouldDeletePost() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);

        mockMvc.perform(delete("/api/posts/{postId}", postId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void delete_shouldThrowExceptionIfPostWasNotFound() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void incrementLike_shouldIncrementLikesByThree() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
            "where p.title = 'Second post'", UUID.class);

        mockMvc.perform(post("/api/posts/{postId}/likes", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesCount").value(1));
        mockMvc.perform(post("/api/posts/{postId}/likes", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesCount").value(2));
        mockMvc.perform(post("/api/posts/{postId}/likes", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesCount").value(3));
    }

    @Test
    public void incrementLike_shouldThrowExceptionIfPostWasNotFound() throws Exception {
        mockMvc.perform(post("/api/posts/{postId}/likes", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void saveImage_shouldSaveFile() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("image",
                "test.jpg", "image/jpg", new byte[]{0, 1, 2, 3});

        mockMvc.perform(multipart("/api/posts/{postId}/image", postId)
                        .file(mockMultipartFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());
    }

    @Test
    public void saveImage_shouldThrowExceptionIfPostNotFound() throws Exception {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("image",
                "test.jpg", "image/jpg", new byte[]{0, 1, 2, 3});

        mockMvc.perform(multipart("/api/posts/{postId}/image", UUID.randomUUID())
                        .file(mockMultipartFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void saveImage_shouldThrowExceptionIfFileIsNull() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);

        mockMvc.perform(multipart("/api/posts/{postId}/image", postId)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getImage_shouldReturnImage() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);
        jdbcTemplate.update("insert into files (id, filename, content, post_id) " +
                "values (gen_random_uuid(), 'test.jpg', ?, ?)", new byte[]{0, 1, 2, 3}, postId);

        mockMvc.perform(get("/api/posts/{postId}/image", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(new byte[]{0, 1, 2, 3}));
    }

    @Test
    public void getImage_shouldThrowExceptionIfFileNotFound() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'Second post'", UUID.class);

        mockMvc.perform(get("/api/posts/{postId}/image", postId))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void getAllByPostId_shouldReturnTwoComments() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);

        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*]", hasSize(2)));
    }
    
    @Test
    public void getAllByPostId_shouldThrowExceptionIfPostNotExists() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void getAllByPostId_shouldReturnZeroComments() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'Second post'", UUID.class);

        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*]", empty()));
    }

    @Test
    public void getByIdAndPostId_shouldReturnComment() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);
        var commentId = jdbcTemplate.queryForObject("select c.id from comments c " +
                "where c.text = 'First comment text'", UUID.class);

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", postId, commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text").value("First comment text"));
    }

    @Test
    public void getByIdAndPostId_shouldThrowExceptionIfCommentWasNotFound() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", postId, UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Comment was not found"));
    }

    @Test
    public void getByIdAndPostId_shouldThrowExceptionIfPostWasNotFound() throws Exception {
        var commentId = jdbcTemplate.queryForObject("select c.id from comments c " +
                "where c.text = 'First comment text'", UUID.class);

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", UUID.randomUUID(), commentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }
    
    @Test
    public void createComment_shouldCreateAndReturnComment() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);
        var request = "{\"postId\":\"" + postId + "\",\"text\":\"Test comment\"}";

        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                .contentType(APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text").value("Test comment"));
    }

    @Test
    public void createComment_shouldThrowExceptionIfPostNotFound() throws Exception {
        var uuid = UUID.randomUUID();
        var request = "{\"postId\":\"" + uuid + "\",\"text\":\"Test comment\"}";

        mockMvc.perform(post("/api/posts/{postId}/comments", uuid)
                .contentType(APPLICATION_JSON)
                .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void createComment_shouldThrowExceptionIfPostIdIsNull() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);
        var request = "{\"postId\":null,\"text\":\"Test comment\"}";

        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("The provided identifiers do not match"));
    }

    @Test
    public void createComment_shouldThrowExceptionIfTextIsEmpty() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);
        var request = "{\"postId\":\"" + postId + "\",\"text\":\"\"}";

        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Required text field is not present"));
    }

    @Test
    public void updateComment_shouldUpdateComment() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);
        var commentId = jdbcTemplate.queryForObject("select c.id from comments c " +
                "where c.text = 'First comment text'", UUID.class);
        var request = "{\"postId\":\"" + postId + "\",\"text\":\"Updated comment\"}";

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                .contentType(APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated comment"));
    }

    @Test
    public void updateComment_shouldThrowExceptionIfPostIsNull() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);
        var commentId = jdbcTemplate.queryForObject("select c.id from comments c " +
                "where c.text = 'First comment text'", UUID.class);
        var request = "{\"postId\":" + null + ",\"text\":\"Updated comment\"}";

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("The provided identifiers do not match"));
    }

    @Test
    public void updateComment_shouldThrowExceptionIfTextIsEmpty() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);
        var commentId = jdbcTemplate.queryForObject("select c.id from comments c " +
                "where c.text = 'First comment text'", UUID.class);
        var request = "{\"postId\":\"" + postId + "\",\"text\":\"\"}";

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Required text field is not present"));
    }

    @Test
    public void updateComment_shouldThrowExceptionIfPostWasNotFound() throws Exception {
        var uuid = UUID.randomUUID();
        var commentId = jdbcTemplate.queryForObject("select c.id from comments c " +
                "where c.text = 'First comment text'", UUID.class);
        var request = "{\"postId\":\"" + uuid + "\",\"text\":\"Updated comment\"}";

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", uuid, commentId)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void deleteComment_shouldDeleteComment() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);
        var commentId = jdbcTemplate.queryForObject("select c.id from comments c " +
                "where c.text = 'First comment text'", UUID.class);

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteComment_shouldThrowExceptionIfPostWasNotFound() throws Exception {
        var commentId = jdbcTemplate.queryForObject("select c.id from comments c " +
                "where c.text = 'First comment text'", UUID.class);

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", UUID.randomUUID(), commentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Requested post was not found"));
    }

    @Test
    public void deleteComment_shouldThrowExceptionIfCommentWasNotFound() throws Exception {
        var postId = jdbcTemplate.queryForObject("select p.id from posts p " +
                "where p.title = 'First post'", UUID.class);

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
