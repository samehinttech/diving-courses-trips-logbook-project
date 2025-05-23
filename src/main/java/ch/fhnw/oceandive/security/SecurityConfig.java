package ch.fhnw.oceandive.security;

import ch.fhnw.oceandive.model.UserDetailsServiceImpl;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Value("${JWT_SECRET}")
  private String jwtKey;

  @Bean
  public UserDetailsService userDetailsService() {
    return new UserDetailsServiceImpl();
  }

 @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      return http
          .csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/api/admin/**").hasRole("ADMIN") // Admin-only endpoints
              .requestMatchers("/api/user/**").hasRole("USER")   // User-only endpoints
              .requestMatchers("/api/guest/**").hasRole("GUEST") // Guest-only endpoints
              .requestMatchers("/api/auth/token").hasAnyRole("USER", "ADMIN") // Token generation endpoint
              .requestMatchers("/api/trips/**", "/api/courses/**").permitAll() // Public endpoints
              .requestMatchers("/api/auth/premium/register").permitAll() // Allow premium user registration
              .requestMatchers("/api/auth/premium/login").permitAll() // Allow premium user login
              .requestMatchers("/api/auth/admin/login").permitAll() // Allow admin login
              .requestMatchers("/api/dive-log/**").hasAnyRole("USER", "ADMIN")
              .anyRequest().authenticated() // All other requests require authentication
          )
          .sessionManagement(session ->
              session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
          .httpBasic(withDefaults())
          .build();
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey.getBytes()));
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    byte[] bytes = jwtKey.getBytes();
    SecretKeySpec originalKey = new SecretKeySpec(bytes, "HmacSHA512");
    return NimbusJwtDecoder.withSecretKey(originalKey).macAlgorithm(MacAlgorithm.HS512).build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService());
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    return authenticationProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
