package com.example.blog_back_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class BlogBackAppApplication {

	static void main(String[] args) {
		var context = SpringApplication.run(BlogBackAppApplication.class, args);

		Arrays.stream(context.getBeanDefinitionNames()).forEach(System.out::println);
	}

}
