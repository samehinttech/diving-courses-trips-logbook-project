package ch.fhnw.oceandive.security;

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

  private final UserDetailsService userDetailsService;

  public SecurityConfig(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter)
      throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .addFilterAfter(jwtAuthFilter,
            org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/user/login", "/api/auth/user/register",
                "/api/auth/refresh", "/api/auth/admin/login").permitAll()
            .requestMatchers("/api/auth/admin/profile").hasRole("ADMIN")
            .requestMatchers("/api/auth/user/profile").hasRole("PREMIUM")
            .requestMatchers("/api/auth/token", "/api/auth/logout").authenticated()
            .requestMatchers("/", "/api", "/api/about", "/api/contact", "/api/privacy-policy",
                "/api/terms-conditions",
                "/api/debug/jwt-info").permitAll()
            .requestMatchers("/api/courses", "/api/courses/**", "/oceandive/api/courses",
                "/oceandive/api/courses/**", "/oceandive/api").permitAll()
            .requestMatchers("/api/trips", "/api/trips/**").permitAll()
            .requestMatchers("/api/bookings/courses/*/guest", "/api/bookings/trips/*/guest")
            .permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/user/**", "/api/dive-logs/**", "/api/bookings/courses/*/user",
                "/api/bookings/trips/*/user").hasRole("PREMIUM")
            .anyRequest().authenticated()
        )
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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
  public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
