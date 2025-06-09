package ch.oceandive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

 // Configuration class for Bean RestTemplate.

@Configuration
public class RestTemplateConfig {

 // Create RestTemplate bean for internal API calls

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
