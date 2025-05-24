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
  public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .addFilterAfter(jwtAuthFilter, org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
            // Auth endpoints
            .requestMatchers("/api/auth/user/login", "/api/auth/user/register", 
                             "/api/auth/refresh", "/api/auth/admin/login").permitAll()

            // Admin-specific auth endpoints
            .requestMatchers("/api/auth/admin/profile").hasRole("ADMIN")

            // Premium user auth endpoints
            .requestMatchers("/api/auth/user/profile").hasRole("PREMIUM")

            // Authenticated auth endpoints
            .requestMatchers("/api/auth/token", "/api/auth/logout").authenticated()

            // Public content endpoints
            .requestMatchers("/", "/api", "/api/about", "/api/contact", "/api/privacy-policy", "/api/terms-conditions", 
                             "/api/debug/jwt-info").permitAll()

            // Public course endpoints
            .requestMatchers("/api/courses", "/api/courses/**", "/oceandive/api/courses", "/oceandive/api/courses/**", "/oceandive/api").permitAll()

            // Public trip endpoints
            .requestMatchers("/api/trips", "/api/trips/**").permitAll()

            // Public booking endpoints for guest users
            .requestMatchers("/api/bookings/courses/*/guest", "/api/bookings/trips/*/guest").permitAll()

            // Admin endpoints
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            // Premium user endpoints
            .requestMatchers("/api/user/**", "/api/dive-logs/**", "/api/bookings/courses/*/user", "/api/bookings/trips/*/user").hasRole("PREMIUM")

            // Any other request requires authentication
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
            JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
            jwt.jwtAuthenticationConverter(converter);
        }))
        .httpBasic(withDefaults())
        .build();
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey.getBytes()));
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    byte[] secret = jwtKey.getBytes();
    SecretKeySpec keySpec = new SecretKeySpec(secret, "HmacSHA512");
    return NimbusJwtDecoder.withSecretKey(keySpec).macAlgorithm(MacAlgorithm.HS512).build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}
