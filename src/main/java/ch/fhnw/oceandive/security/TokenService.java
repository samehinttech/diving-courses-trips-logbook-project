package ch.fhnw.oceandive.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TokenService {

  private final JwtEncoder encoder;
  private final JwtDecoder decoder;
  // In-memory store for blocklisted tokens
  private final Set<String> blacklistedTokens = Collections.newSetFromMap(new ConcurrentHashMap<>());

  public TokenService(JwtEncoder encoder, JwtDecoder decoder) {
    this.encoder = encoder;
    this.decoder = decoder;
  }

  public String generateToken(Authentication authentication) {
    Instant now = Instant.now();
    String scope = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(authority -> !authority.startsWith("ROLE"))
        .collect(Collectors.joining(" "));
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(now)
        .expiresAt(now.plus(1, ChronoUnit.HOURS))
        .subject(authentication.getName())
        .claim("scope", scope)
        .build();
    var encoderParameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS512).build(), claims);
    return this.encoder.encode(encoderParameters).getTokenValue();
  }

  public String generateRefreshToken(String username) {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(now)
        .expiresAt(now.plus(24, ChronoUnit.HOURS)) // Refresh token valid for 24 hours
        .subject(username)
        .build();
    var encoderParameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS512).build(), claims);
    return this.encoder.encode(encoderParameters).getTokenValue();
  }

  public JwtDecoder getJwtDecoder() {
    return this.decoder;
  }

  /**
   * Add a token to the blacklist
   * @param token The token to blacklist
   */
  public void blacklistToken(String token) {
    blacklistedTokens.add(token);
  }

  /**
   * Check if a token is blacklisted
   * @param token The token to check
   * @return true if the token is blacklisted, false otherwise
   */
  public boolean isTokenBlacklisted(String token) {
    return blacklistedTokens.contains(token);
  }

  /**
   * Validate a token
   * @param token The token to validate
   * @return true if the token is valid, false otherwise
   */
  public boolean validateToken(String token) {
    if (isTokenBlacklisted(token)) {
      return false;
    }

    try {
      decoder.decode(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
