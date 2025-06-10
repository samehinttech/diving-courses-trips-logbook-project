package ch.oceandive.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Serve static resources from the classpath
    registry.addResourceHandler("/static/**")
        .addResourceLocations("classpath:/static/");
    registry.addResourceHandler("/images/**")
        .addResourceLocations("classpath:/static/images/");
    registry.addResourceHandler("/css/**")
        .addResourceLocations("classpath:/static/css/");
    registry.addResourceHandler("/js/**")
        .addResourceLocations("classpath:/static/js/");
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
    registry.addResourceHandler("/templates/**")
        .addResourceLocations("classpath:/templates/**");
    registry.addResourceHandler("/fragments/**")
        .addResourceLocations("classpath:/templates/fragments/");
    registry.addResourceHandler("reset/**")
        .addResourceLocations("classpath:/templates/reset/");
  }
}