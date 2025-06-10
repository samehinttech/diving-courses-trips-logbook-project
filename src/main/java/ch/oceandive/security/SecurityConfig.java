package ch.oceandive.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.util.Base64;
import java.util.stream.Collectors;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Value("${JWT_SECRET}")
  private String jwtKey;

  private final JwtAuthenticationConverter jwtAuthenticationConverter;

  public SecurityConfig(JwtAuthenticationConverter jwtAuthenticationConverter) {
    this.jwtAuthenticationConverter = jwtAuthenticationConverter;
  }

  @Bean
  @Order(1)
  public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/api/**")
        // CSRF disabled for API endpoints (Since they use JWT tokens anyway)
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/auth/user/login", "/api/auth/user/register", "/api/auth/refresh", "/api/auth/admin/login",
                "/api/about", "/api/contact", "/api/privacy", "/api/terms", "/api/debug/**",
                "/api/courses/**", "/api/trips/**","/api/index"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
    // Custom CSRF token request handler
    CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
    // Set the name of the attribute the CsrfToken will be on
    requestHandler.setCsrfRequestAttributeName("_csrf");

    http
        // Enable CSRF protection for web pages
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(requestHandler)
            // Exclude H2 console from CSRF (for development) TODO remove in production
            .ignoringRequestMatchers("/h2-console/**")
        )
        .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
            .requestMatchers("/", "/courses", "/trips", "/about", "/contact/**", "/privacy", "/terms", "/error/**").permitAll()
            .requestMatchers("/register", "/login", "/forgot-password", "/reset-password", "/reset/**", "/h2-console/**").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/dive-log/**").hasRole("PREMIUM")
            .requestMatchers("/user-profile/**", "/profile-edit","/my-profile/","/my-profile/update").hasAnyRole("PREMIUM", "ADMIN")
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .usernameParameter("username")
            .passwordParameter("password")
            .successHandler((request, response, authentication) -> {
              var roles = authentication.getAuthorities().stream()
                  .map(GrantedAuthority::getAuthority)
                  .collect(Collectors.toSet());
              if (roles.contains("ROLE_ADMIN")) {
                response.sendRedirect("/admin/dashboard");
              } else if (roles.contains("ROLE_PREMIUM")) {
                response.sendRedirect("/dive-log");
              } else {
                response.sendRedirect("/");
              }
            })
            .failureUrl("/login?error=true")
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout=true")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID", "XSRF-TOKEN")
            .permitAll()
        );
    return http.build();
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    byte[] secret = Base64.getDecoder().decode(jwtKey);
    return new NimbusJwtEncoder(new ImmutableSecret<>(secret));
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    byte[] secret = Base64.getDecoder().decode(jwtKey);
    SecretKeySpec keySpec = new SecretKeySpec(secret, "HmacSHA512");
    return NimbusJwtDecoder.withSecretKey(keySpec).macAlgorithm(MacAlgorithm.HS512).build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    authProvider.setHideUserNotFoundExceptions(false);
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}