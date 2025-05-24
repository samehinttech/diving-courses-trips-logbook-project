package ch.fhnw.oceandive.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TokenService {

  private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

  private final JwtEncoder encoder;
  private final JwtDecoder decoder;

  @Value("${oceandive.security.jwt.access-token.expiration:3600}")
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
   */
  public String generateToken(Authentication authentication) {
    Instant now = Instant.now();
    String tokenId = UUID.randomUUID().toString();

    // Collect user roles from an authentication object
    List<String> roles = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(jwtIssuer)
        .issuedAt(now)
        .expiresAt(now.plus(accessTokenExpirationSeconds, ChronoUnit.SECONDS))
        .subject(authentication.getName())
        .id(tokenId)
        .claim("roles", roles) // Add user roles to token claims
        .build();

    JwtEncoderParameters encoderParameters = JwtEncoderParameters.from(
        JwsHeader.with(MacAlgorithm.HS512).build(),
        claims
    );

    String token = this.encoder.encode(encoderParameters).getTokenValue();
    logger.debug("Generated access token for user '{}': {}", authentication.getName(), token);
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
    logger.debug("Generated refresh token for user '{}': {}", username, token);
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
        logger.debug("Remaining validity for token '{}': {} seconds", jwt.getId(), remainingTime);
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
        logger.warn("Token validation failed: token is expired.");
        return false;
      }

      String issuer = String.valueOf(jwt.getIssuer());
      if (!jwtIssuer.equals(issuer)) {
        logger.warn("Token validation failed: invalid issuer.");
        return false;
      }

      return true;

    } catch (Exception e) {
      logger.error("Token validation failed. Error: {}", e.getMessage());
      return false;
    }
  }
  
  /**
   * Validate a token and check if it has the required role.
   */
  public boolean validateTokenWithRole(String token, String requiredRole) {
    if (!validateToken(token)) {
      return false;
    }
  
    try {
      Jwt jwt = decoder.decode(token);
      List<String> roles = null;
      
      // Try to get roles as string list first
      try {
          roles = jwt.getClaimAsStringList("roles");
      } catch (Exception e) {
          // If that fails, try to get as a generic claim and convert
          Object rolesObj = jwt.getClaim("roles");
          logger.debug("Roles from JWT raw: {} (type: {})", 
                      rolesObj, 
                      rolesObj != null ? rolesObj.getClass().getName() : "null");
                      
          if (rolesObj instanceof List) {
              roles = ((List<?>) rolesObj).stream()
                      .map(Object::toString)
                      .collect(Collectors.toList());
          }
      }
      
      logger.debug("Validating token with roles: {} against required role: {}", roles, requiredRole);
      
      if (roles == null || !roles.contains(requiredRole)) {
        logger.warn("Token validation failed: missing required role '{}'", requiredRole);
        return false;
      }
      
      return true;
    } catch (Exception e) {
      logger.error("Role validation failed. Error: {}", e.getMessage(), e);
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
      logger.info("Token blacklisted: {}. Total blacklisted tokens: {}", jwt.getId(), blacklistedTokens.size());
    } catch (Exception e) {
      logger.error("Failed to blacklist token. Error: {}", e.getMessage());
    }
  }

  /**
   * Check if a token is blocklisted.
   */
  public boolean isTokenBlacklisted(String token) {
    return blacklistedTokens.containsKey(token);
  }

  /**
   * Clean up expired tokens in the blocklist.
   */
  @Scheduled(fixedRateString = "${oceandive.security.jwt.blacklist-cleanup-interval:3600000}")
  public void cleanupBlacklist() {
    Instant now = Instant.now();
    int originalSize = blacklistedTokens.size();
    blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    int cleaned = originalSize - blacklistedTokens.size();
    if (cleaned > 0) {
      logger.debug("Blacklist cleanup: removed {} expired tokens. Remaining: {}", cleaned, blacklistedTokens.size());
    }
  }
}
