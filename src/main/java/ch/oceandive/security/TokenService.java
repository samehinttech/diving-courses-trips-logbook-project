package ch.oceandive.security;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

  private final JwtEncoder encoder;
  private final JwtDecoder decoder;

  // FIXED: Changed default from 900 (15 min) to 7200 (2 hours) for better UX
  @Value("${oceandive.security.jwt.access-token.expiration:7200}")
  private long accessTokenExpirationSeconds;

  @Value("${oceandive.security.jwt.refresh-token.expiration:86400}")
  private long refreshTokenExpirationSeconds;

  @Value("${oceandive.security.jwt.issuer:oceandive-api}")
  private String jwtIssuer;

  private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

  public TokenService(JwtEncoder encoder, JwtDecoder decoder) {
    this.encoder = encoder;
    this.decoder = decoder;
  }

  /**
   * Generate a JWT access token with user roles included.
   * FIXED: Removes ROLE_ prefix for cleaner JWT payload
   */
  public String generateToken(Authentication authentication) {
    Instant now = Instant.now();
    String tokenId = UUID.randomUUID().toString();

    // FIXED: Remove ROLE_ prefix from roles for cleaner JWT storage
    List<String> roles = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role) // Remove ROLE_ prefix
        .collect(Collectors.toList());

    logger.debug("Generating JWT for user '{}' with clean roles: {} (removed ROLE_ prefix)",
        authentication.getName(), roles);

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(jwtIssuer)
        .issuedAt(now)
        .expiresAt(now.plus(accessTokenExpirationSeconds, ChronoUnit.SECONDS))
        .subject(authentication.getName())
        .id(tokenId)
        .claim("roles", roles) // Store clean roles: ["ADMIN"] instead of ["ROLE_ADMIN"]
        .build();

    JwtEncoderParameters encoderParameters = JwtEncoderParameters.from(
        JwsHeader.with(MacAlgorithm.HS512).build(),
        claims
    );

    String token = this.encoder.encode(encoderParameters).getTokenValue();
    logger.info("Generated access token for user '{}' with roles: {}, expires in {} seconds ({} hours)",
        authentication.getName(), roles, accessTokenExpirationSeconds, accessTokenExpirationSeconds / 3600.0);
    return token;
  }

  /**
   * Generate a JWT refresh token.
   */
  public String generateRefreshToken(String username) {
    Instant now = Instant.now();
    String tokenId = UUID.randomUUID().toString();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(jwtIssuer)
        .issuedAt(now)
        .expiresAt(now.plus(refreshTokenExpirationSeconds, ChronoUnit.SECONDS))
        .subject(username)
        .id(tokenId)
        .claim("token_type", "refresh")
        .build();

    JwtEncoderParameters encoderParameters = JwtEncoderParameters.from(
        JwsHeader.with(MacAlgorithm.HS512).build(),
        claims
    );

    String token = this.encoder.encode(encoderParameters).getTokenValue();
    logger.debug("Generated refresh token for user '{}', expires in {} hours",
        username, refreshTokenExpirationSeconds / 3600.0);
    return token;
  }

  /**
   * Get the remaining validity time of a token in seconds.
   */
  public long getTokenRemainingValiditySeconds(String token) {
    try {
      Jwt jwt = decoder.decode(token);
      Instant expiration = jwt.getExpiresAt();
      if (expiration != null) {
        Duration duration = Duration.between(Instant.now(), expiration);
        long remainingTime = Math.max(0, duration.getSeconds());
        logger.debug("Token '{}' has {} seconds ({} minutes) remaining",
            jwt.getId(), remainingTime, remainingTime / 60.0);
        return remainingTime;
      }
    } catch (Exception e) {
      logger.warn("Failed to get token validity: {}", e.getMessage());
    }
    return 0;
  }

  /**
   * Extract the username (subject) from a token.
   */
  public String getUsernameFromToken(String token) {
    try {
      Jwt jwt = decoder.decode(token);
      String username = jwt.getSubject();
      logger.debug("Extracted username '{}' from token '{}'", username, jwt.getId());
      return username;
    } catch (Exception e) {
      logger.warn("Failed to extract username from token: {}", e.getMessage());
    }
    return null;
  }

  /**
   * Validate an access or refresh token.
   */
  public boolean validateToken(String token) {
    if (isTokenBlacklisted(token)) {
      logger.warn("Token validation failed: token is blacklisted.");
      return false;
    }

    try {
      Jwt jwt = decoder.decode(token);
      Instant expiresAt = jwt.getExpiresAt();
      if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
        logger.warn("Token validation failed: token '{}' is expired.", jwt.getId());
        return false;
      }

      String issuer = String.valueOf(jwt.getIssuer());
      if (!jwtIssuer.equals(issuer)) {
        logger.warn("Token validation failed: invalid issuer '{}', expected '{}'.", issuer, jwtIssuer);
        return false;
      }

      logger.debug("Token '{}' validation successful for user '{}'", jwt.getId(), jwt.getSubject());
      return true;

    } catch (Exception e) {
      logger.error("Token validation failed. Error: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Blacklist a token.
   */
  public void blacklistToken(String token) {
    try {
      Jwt jwt = decoder.decode(token);
      blacklistedTokens.put(token, jwt.getExpiresAt());
      logger.info("Token '{}' blacklisted for user '{}'. Total blacklisted tokens: {}",
          jwt.getId(), jwt.getSubject(), blacklistedTokens.size());
    } catch (Exception e) {
      logger.error("Failed to blacklist token. Error: {}", e.getMessage());
    }
  }

  /**
   * Check if a token is blacklisted.
   */
  public boolean isTokenBlacklisted(String token) {
    return blacklistedTokens.containsKey(token);
  }

  /**
   * Clean up expired tokens in the blacklist.
   */
  @Scheduled(fixedRateString = "${oceandive.security.jwt.blacklist-cleanup-interval:3600000}")
  public void cleanupBlacklist() {
    Instant now = Instant.now();
    int originalSize = blacklistedTokens.size();
    blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    int cleaned = originalSize - blacklistedTokens.size();
    if (cleaned > 0) {
      logger.info("Blacklist cleanup: removed {} expired tokens. Remaining: {}", cleaned, blacklistedTokens.size());
    }
  }
}