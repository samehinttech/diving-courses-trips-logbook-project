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
  
  // Token configuration with defaults that can be overridden in .env
  @Value("${oceandive.security.jwt.access-token.expiration:3600}")
  private long accessTokenExpirationSeconds;
  
  @Value("${oceandive.security.jwt.refresh-token.expiration:86400}")
  private long refreshTokenExpirationSeconds;

  // In-memory store for blacklisted tokens with expiration time
  private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

  public TokenService(JwtEncoder encoder, JwtDecoder decoder) {
    this.encoder = encoder;
    this.decoder = decoder;
  }

  /**
   * Generate an access token for the authenticated user
   * @return The generated JWT token string
   */
  public String generateToken(Authentication authentication) {
    Instant now = Instant.now();
    
    // Create a unique token ID
    String tokenId = UUID.randomUUID().toString();
    
    String scope = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(authority -> !authority.startsWith("ROLE"))
        .collect(Collectors.joining(" "));
        
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("oceandive-api")
        .issuedAt(now)
        .expiresAt(now.plus(accessTokenExpirationSeconds, ChronoUnit.SECONDS))
        .subject(authentication.getName())
        .id(tokenId)  // Add jti claim for unique token ID
        .claim("scope", scope)
        .build();
        
    var encoderParameters = JwtEncoderParameters.from(
        JwsHeader.with(MacAlgorithm.HS512).build(), 
        claims
    );
    
    String token = this.encoder.encode(encoderParameters).getTokenValue();
    logger.debug("Generated access token for user: {}", authentication.getName());
    return token;
  }

  /**
   * Generate a refresh token for the given username
   * @return The generated refresh token string
   */
  public String generateRefreshToken(String username) {
    Instant now = Instant.now();
    String tokenId = UUID.randomUUID().toString();
    
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("oceandive-api")
        .issuedAt(now)
        .expiresAt(now.plus(refreshTokenExpirationSeconds, ChronoUnit.SECONDS))
        .subject(username)
        .id(tokenId)
        .claim("token_type", "refresh")
        .build();
        
    var encoderParameters = JwtEncoderParameters.from(
        JwsHeader.with(MacAlgorithm.HS512).build(), 
        claims
    );
    
    String token = this.encoder.encode(encoderParameters).getTokenValue();
    logger.debug("Generated refresh token for user: {}", username);
    return token;
  }

  /**
   * Add a token to the blacklist with its expiration time
   * 
   * @param token The token to blacklist
   */
  public void blacklistToken(String token) {
    try {
      Jwt jwt = decoder.decode(token);
      blacklistedTokens.put(token, jwt.getExpiresAt());
      logger.debug("Token blacklisted. Total blacklisted tokens: {}", blacklistedTokens.size());
    } catch (Exception e) {
      logger.warn("Failed to blacklist invalid token: {}", e.getMessage());
    }
  }

  /**
   * Blacklist all tokens for a specific user
   * 
   * @param username The username whose tokens should be blacklisted
   */
  public void blacklistUserTokens(String username) {
    logger.info("Blacklisting all tokens for user: {}", username);
  }

  /**
   * Check if a token is blacklisted
   * @return true if the token is blacklisted, false otherwise
   */
  public boolean isTokenBlacklisted(String token) {
    return blacklistedTokens.containsKey(token);
  }

  /**
   * Clean up expired tokens from the blacklist
   * Runs every hour by default
   */
  @Scheduled(fixedRateString = "${oceandive.security.jwt.blacklist-cleanup-interval:3600000}")
  public void cleanupBlacklist() {
    Instant now = Instant.now();
    int sizeBefore = blacklistedTokens.size();
    
    // Remove expired tokens
    blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    
    int removed = sizeBefore - blacklistedTokens.size();
    if (removed > 0) {
      logger.info("Cleaned up {} expired tokens from blacklist. Remaining: {}", 
          removed, blacklistedTokens.size());
    }
  }

  /**
   * Validate a token
   * @return true if the token is valid, false otherwise
   */
  public boolean validateToken(String token) {
    if (isTokenBlacklisted(token)) {
      logger.debug("Token validation failed: token is blacklisted");
      return false;
    }

    try {
      Jwt jwt = decoder.decode(token);
      
      // Validate expiration time
      Instant expiresAt = jwt.getExpiresAt();
      if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
        logger.debug("Token validation failed: token is expired");
        return false;
      }
      
      // Validate issuer
      String issuer = jwt.getClaimAsString("iss");
      if (!"oceandive-api".equals(issuer)) {
        logger.debug("Token validation failed: invalid issuer");
        return false;
      }
      
      return true;
    } catch (JwtValidationException e) {
      logger.debug("Token validation failed: {}", e.getMessage());
      return false;
    } catch (Exception e) {
      logger.warn("Unexpected error during token validation: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Parse a token and extract its claims
   * @return The JWT object containing all claims
   * @throws JwtException if the token is invalid
   */
  public Jwt parseToken(String token) throws JwtException {
    return decoder.decode(token);
  }
  
  /**
   * Get remaining validity time of a token in seconds
   * @return Remaining seconds until expiration, or 0 if expired/invalid
   */
  public long getTokenRemainingValiditySeconds(String token) {
    try {
      Jwt jwt = decoder.decode(token);
      Instant expiration = jwt.getExpiresAt();
      if (expiration != null) {
        Duration duration = Duration.between(Instant.now(), expiration);
        return Math.max(0, duration.getSeconds());
      }
    } catch (Exception e) {
      logger.debug("Could not determine token validity: {}", e.getMessage());
    }
    return 0;
  }
  
  /**
   * Extract username from token
   * @return The username, or null if token is invalid
   */
  public String getUsernameFromToken(String token) {
    try {
      Jwt jwt = decoder.decode(token);
      return jwt.getSubject();
    } catch (Exception e) {
      return null;
    }
  }
}