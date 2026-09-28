package com.example.bookapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookApiOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Book Management REST API")
                .description("Spring Boot + MySQL CRUD API for managing books")
                .version("1.0.0"));
    }
}
