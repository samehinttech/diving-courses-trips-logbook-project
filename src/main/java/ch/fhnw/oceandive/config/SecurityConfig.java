package ch.fhnw.oceandive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configure security settings
        http
            // Disable CSRF for H2 console
            .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow H2 console access without authentication
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                // Secure all other endpoints
                .anyRequest().authenticated()
            )
            // Configure headers to allow the H2 console to work properly
            .headers(headers -> headers
                .frameOptions(FrameOptionsConfig::sameOrigin)
            );
            
        return http.build();
    }
}
