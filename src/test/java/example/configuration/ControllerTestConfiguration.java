package example.configuration;

import com.example.controller.ExceptionController;
import com.example.controller.MainController;
import com.example.service.CommentService;
import com.example.service.FileService;
import com.example.service.PostService;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = "com.example",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {
                org.springframework.stereotype.Service.class,
                org.springframework.stereotype.Repository.class,
                org.springframework.stereotype.Component.class
        })
)
@Import(RootTestConfiguration.class)
public class ControllerTestConfiguration {

    @Bean
    @Profile("test")
    public MainController mainController(PostService postService,
                                         CommentService commentService,
                                         FileService fileService) {
        return new MainController(postService, commentService, fileService);
    }

    @Bean
    @Profile("test")
    public ExceptionController exceptionController() {
        return new ExceptionController();
    }
}
