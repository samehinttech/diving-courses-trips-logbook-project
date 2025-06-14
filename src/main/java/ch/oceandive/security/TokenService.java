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

  @Value("${jwt_access_token_expiration}")
  private long accessTokenExpirationSeconds;

  @Value("${jwt_refresh_token_expiration}")
  private long refreshTokenExpirationSeconds;

  @Value("${jwt_issuer}")
  private String jwtIssuer;

  private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

  public TokenService(JwtEncoder encoder, JwtDecoder decoder) {
    this.encoder = encoder;
    this.decoder = decoder;
  }

  /**
   * Generate a JWT access token with user roles included.
   * I had still to add manually the ROLE_ prefix to the database roles, which I couldn't understand what was the reason for that.
   * So I decided to remove it here for cleaner JWT storage.
   */
  public String generateToken(Authentication authentication) {
    Instant now = Instant.now();
    String tokenId = UUID.randomUUID().toString();

    List<String> roles = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role) // Remove ROLE_ prefix
        .collect(Collectors.toList());

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(jwtIssuer)
        .issuedAt(now)
        .expiresAt(now.plus(accessTokenExpirationSeconds, ChronoUnit.SECONDS))
        .subject(authentication.getName())
        .id(tokenId)
        .claim("roles", roles)
        .build();

    JwtEncoderParameters encoderParameters = JwtEncoderParameters.from(
        JwsHeader.with(MacAlgorithm.HS512).build(),
        claims
    );
    return this.encoder.encode(encoderParameters).getTokenValue();
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
    return this.encoder.encode(encoderParameters).getTokenValue();
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
        return Math.max(0, duration.getSeconds());
      }
    } catch (Exception e) {
      logger.warn("Failed to get token validity: {}", e.getMessage());
    }
    return 0;
  }

  /**
   * Extract the username from a token.
   */
  public String getUsernameFromToken(String token) {
    try {
      Jwt jwt = decoder.decode(token);
      return jwt.getSubject();
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
      return false;
    }
    try {
      Jwt jwt = decoder.decode(token);
      Instant expiresAt = jwt.getExpiresAt();
      if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
        return false;
      }
      String issuer = String.valueOf(jwt.getIssuer());
      return jwtIssuer.equals(issuer);
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
  @Scheduled(fixedRateString = "${jwt_blackList_cleanup_interval}")
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