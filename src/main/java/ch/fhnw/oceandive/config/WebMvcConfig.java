package ch.fhnw.oceandive.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for serving uploaded images through a static resource handler
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
       // To save the uploaded images in the db_assets folder
        registry.addResourceHandler("/db_assets/**")
                .addResourceLocations("file:./db_assets/");
    }
}
