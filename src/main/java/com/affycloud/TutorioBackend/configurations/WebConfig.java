package com.affycloud.TutorioBackend.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000") // React development server URL
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve images from the uploads/images directory
        registry.addResourceHandler("/api/images/view/**")
                .addResourceLocations("file:uploads/images/");

                registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:uploads/thumbnails/");
        registry.addResourceHandler("/api/books/images/view/**")
                .addResourceLocations("file:uploads/books/images/");
        registry.addResourceHandler("/api/blogs/images/view/**")
                .addResourceLocations("file:uploads/blogs/images/");
        registry.addResourceHandler("/api/tests/images/view/**")
                .addResourceLocations("file:uploads/tests/images/");
    }
}

