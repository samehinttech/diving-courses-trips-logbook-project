package ch.oceandive.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
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
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Value("${jwt_secret}")
  private String jwtKey;

  @Value("${app.base-url}")
  private String appBaseUrl;

  private final JwtAuthenticationConverter jwtAuthenticationConverter;

  public SecurityConfig(JwtAuthenticationConverter jwtAuthenticationConverter) {
    this.jwtAuthenticationConverter = jwtAuthenticationConverter;
  }

  @Bean
  @Order(1)
  public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/api/**")
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .headers(headers -> headers
            .frameOptions(FrameOptionsConfig::deny) // Disable frame options for API, enable to use H2 console
            .contentTypeOptions(contentTypeOptionsConfig -> {})
            .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                .maxAgeInSeconds(31536000) // 1 year
                .includeSubDomains(true)
                .preload(true))
            .referrerPolicy(referrerPolicyConfig -> referrerPolicyConfig.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        )
        .authorizeHttpRequests(auth -> auth
            // Authentication endpoints
            .requestMatchers(
                "/api/auth/user/login",
                "/api/auth/user/register",
                "/api/auth/refresh",
                "/api/auth/admin/login",
                "/api/auth/admin/register"
            ).permitAll()
            // Public content endpoints
            .requestMatchers(
                "/api/about",
                "/api/contact",
                "/api/privacy",
                "/api/terms",
                "/api/index",
                "/api/health" // Health check endpoint
            ).permitAll()
            // Public course endpoints
            .requestMatchers(HttpMethod.GET,
                "/api/courses",
                "/api/courses/{id}",
                "/api/courses/upcoming",
                "/api/courses/available",
                "/api/courses/name/{name}",
                "/api/courses/date-range"
            ).permitAll()
            // Public trip endpoints
            .requestMatchers(HttpMethod.GET,
                "/api/trips",
                "/api/trips/{id}",
                "/api/trips/slug/{slug}",
                "/api/trips/search",
                "/api/trips/upcoming",
                "/api/trips/available",
                "/api/trips/featured",
                "/api/trips/location/{location}",
                "/api/trips/date-range",
                "/api/trips/certification/{level}",
                "/api/trips/price-range",
                "/api/trips/{id}/similar",
                "/api/trips/analytics/popular-destinations",
                "/api/trips/locations",
                "/api/trips/active",
                "/api/trips/past"
            ).permitAll()
            // API documentation (disable in production if needed)
            .requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/webjars/**"
            ).permitAll()
            // Protected endpoints
            .requestMatchers("/api/dive-logs/**").hasAnyRole("PREMIUM", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/trips/{id}/book").hasAnyRole("PREMIUM", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/trips/{id}/cancel-booking")
            .hasAnyRole("PREMIUM", "ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/trips/{id}/can-book")
            .hasAnyRole("PREMIUM", "ADMIN")

            // Admin-only endpoints
            .requestMatchers(HttpMethod.POST, "/api/courses/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/trips").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/trips/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/trips/**").hasRole("ADMIN")
            .requestMatchers("/api/trips/{id}/archive").hasRole("ADMIN")
            .requestMatchers("/api/trips/statistics").hasRole("ADMIN")
            .requestMatchers("/api/trips/analytics/most-booked").hasRole("ADMIN")
            .requestMatchers("/api/trips/analytics/low-bookings").hasRole("ADMIN")

            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(
            jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
    CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
    requestHandler.setCsrfRequestAttributeName("_csrf");

    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
        )
        .headers(headers -> headers
            .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
            .contentTypeOptions(ContentTypeOptionsConfig-> {})
            .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                .maxAgeInSeconds(31536000)
                .includeSubDomains(true)
                .preload(true))
            .referrerPolicy(referrerPolicyConfig -> referrerPolicyConfig.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        )
        .authorizeHttpRequests(auth -> auth
            // Static resources
            .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
            // Public pages
            .requestMatchers("/", "/courses", "/trips", "/about", "/contact/**", "/privacy",
                "/terms", "/error/**", "/not-available", "/400", "/403", "/404", "/409", "/500").permitAll()
            // Authentication pages
            .requestMatchers("/register", "/login", "/forgot-password", "/reset-password",
                "/reset/**").permitAll()
            .requestMatchers("/h2-console/**").permitAll() // uncomment if you need H2 console
            // Health check endpoints
            .requestMatchers("/health", "/actuator/health").permitAll()

            // Protected areas
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/dive-log/**").hasAnyRole("PREMIUM", "ADMIN")
            .requestMatchers("/user-profile/**", "/profile-edit", "/my-profile/",
                "/my-profile/update").hasAnyRole("PREMIUM", "ADMIN")
            .requestMatchers("/admin-dashboard", "/dive-log", "/admin-profile",
                "/admin-profile/edit", "/admin/register", "/admin-profile/update").hasRole("ADMIN")
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
                response.sendRedirect("/admin-dashboard");
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
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();


    configuration.setAllowedOriginPatterns(Arrays.asList(
        appBaseUrl,
        "https://*.onrender.com" // Render
        // "https://editor.swagger.io" // Uncomment if using Swagger UI
    ));

    configuration.setAllowedMethods(java.util.Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
    ));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
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
    // Increased strength for production
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder,
      UserDetailsService userDetailsService) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    authProvider.setHideUserNotFoundExceptions(false);
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}