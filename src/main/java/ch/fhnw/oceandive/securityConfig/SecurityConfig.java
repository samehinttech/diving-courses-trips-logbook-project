package ch.fhnw.oceandive.securityConfig;

import static org.springframework.security.config.Customizer.withDefaults;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;

import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Configuration
@EnableWebSecurity
@Component("securityConfig")
public class SecurityConfig {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.algorithm}")
  private String jwtAlgorithm;

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(CustomUserDetailsImpl userDetailsService) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return new ProviderManager(authProvider);
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    SecretKeySpec secretKey = new SecretKeySpec(secretBytes, jwtAlgorithm);
    ImmutableSecret<SecurityContext> secret = new ImmutableSecret<>(secretKey);
    return new NimbusJwtEncoder(secret);
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    MacAlgorithm algorithm = MacAlgorithm.valueOf(jwtAlgorithm);
    SecretKeySpec secretKey = new SecretKeySpec(secretBytes, algorithm.getName());
    return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(algorithm).build();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      return http
          .csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/","/h2-console/**", "/api/auth/login", "/api/auth/register").permitAll()
              .requestMatchers("/api/trips/**", "/api/courses/**").permitAll()
              .requestMatchers("/api/certifications/**").permitAll()
              .requestMatchers("/api/public/**").permitAll()
              .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
              .requestMatchers("/api/user/**").hasAnyAuthority("ROLE_USER_ACCOUNT", "ROLE_ADMIN")
              .anyRequest().authenticated()
          )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
        .headers(headers ->
            headers.frameOptions(FrameOptionsConfig::sameOrigin)) // For H2 console
        .build();
  }

}
