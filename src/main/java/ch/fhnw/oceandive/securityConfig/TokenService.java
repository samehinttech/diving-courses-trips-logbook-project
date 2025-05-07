package ch.fhnw.oceandive.securityConfig;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  @Value("${jwt.issuer:self}")
  private String ISSUER;

  @Value("${jwt.audience:users}")
  private String AUDIENCE;

  @Value("${jwt.expiration:3600}")
  private long EXPIRATION_DURATION;

  @Value("${jwt.algorithm:HS512}")
  private String ALGORITHM;

  // Default time before expiration to consider a token as "about to expire" (in seconds)
  @Value("${jwt.refresh-window:300}")
  private long REFRESH_WINDOW;

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;

  // Simple in-memory blocklist for invalidated tokens
  private final Set<String> tokenBlocklist = new HashSet<>();

  public TokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
    if (jwtEncoder == null) {
      throw new IllegalArgumentException("JwtEncoder must not be null");
    }
    if (jwtDecoder == null) {
      throw new IllegalArgumentException("JwtDecoder must not be null");
    }
    this.jwtEncoder = jwtEncoder;
    this.jwtDecoder = jwtDecoder;
  }

  public String generateToken(Authentication authentication) {
    Instant now = Instant.now();
    String scope = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(authority -> !authority.startsWith("ROLE_"))
        .collect(Collectors.joining(" "));
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(ISSUER)
        .audience(List.of(AUDIENCE))
        .issuedAt(now)
        .expiresAt(now.plus(EXPIRATION_DURATION, ChronoUnit.SECONDS))
        .subject(authentication.getName())
        .claim("scope", scope)
        .build();
    var encodeParameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.valueOf(
            ALGORITHM)).build(),
        claims);
    return this.jwtEncoder.encode(encodeParameters).getTokenValue();
  }

  /**
   * Checks if a token is in the blocklist (invalidated)
   * 
   * @param token the token to check
   * @return true if the token is in the blocklist, false otherwise
   */
  public boolean isTokenBlocklisted(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }

    // Remove "Bearer " prefix if present
    if (token.startsWith("Bearer ")) {
      token = token.substring(7);
    }

    return tokenBlocklist.contains(token);
  }

  /**
   * Invalidates a token by adding it to the blocklist
   * 
   * @param token the token to invalidate
   * @return true if the token was added to the blocklist, false if it was already there
   */
  public boolean invalidateToken(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }

    // Remove "Bearer " prefix if present
    if (token.startsWith("Bearer ")) {
      token = token.substring(7);
    }

    return tokenBlocklist.add(token);
  }

  /**
   * Refreshes a token if it's about to expire
   * 
   * @param token the token to refresh
   * @return a new token if the original is about to expire, or the original token if it's still valid
   * @throws IllegalArgumentException if the token is invalid, expired, or has been invalidated
   */
  public String refreshToken(String token) {
    if (token == null || token.isEmpty()) {
      throw new IllegalArgumentException("Token cannot be null or empty");
    }

    // Remove "Bearer " prefix if present
    if (token.startsWith("Bearer ")) {
      token = token.substring(7);
    }

    // Check if token is in the blocklist
    if (isTokenBlocklisted(token)) {
      throw new IllegalArgumentException("Token has been invalidated");
    }

    try {
      // 1. Parse and validate the existing token
      Jwt jwt = jwtDecoder.decode(token);

      // Extract subject and claims from the token
      String subject = jwt.getSubject();
      Map<String, Object> existingClaims = jwt.getClaims();

      // 2. Check if the token is expired or about to expire
      Instant expiresAt = jwt.getExpiresAt();
      Instant now = Instant.now();

      // If token is already expired, throw an exception
      if (expiresAt != null && expiresAt.isBefore(now)) {
        throw new IllegalArgumentException("Token has expired");
      }

      // Check if token is about to expire (within the refresh window)
      boolean isAboutToExpire = expiresAt != null && 
          expiresAt.isBefore(now.plus(REFRESH_WINDOW, ChronoUnit.SECONDS));

      // If token is not about to expire, return the original token
      if (!isAboutToExpire) {
        return token; // Token is still valid and not about to expire
      }

      // 3. Create a new token with a new expiration time
      JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
          .issuer(ISSUER)
          .audience(List.of(AUDIENCE))
          .issuedAt(now)
          .expiresAt(now.plus(EXPIRATION_DURATION, ChronoUnit.SECONDS))
          .subject(subject);

      // Copy relevant claims from the original token
      existingClaims.forEach((key, value) -> {
        // Skip standard claims that we've already set
        if (!key.equals("iss") && !key.equals("sub") && !key.equals("aud") 
            && !key.equals("exp") && !key.equals("iat") && !key.equals("jti")) {
          claimsBuilder.claim(key, value);
        }
      });

      // Add a refreshed claim to indicate this is a refreshed token
      claimsBuilder.claim("refreshed", true);

      JwtClaimsSet claims = claimsBuilder.build();

      // 4. Optionally blocklist the old token
      tokenBlocklist.add(token);

      var encodeParameters = JwtEncoderParameters.from(
          JwsHeader.with(MacAlgorithm.valueOf(ALGORITHM)).build(),
          claims);

      return this.jwtEncoder.encode(encodeParameters).getTokenValue();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid token: " + e.getMessage(), e);
    }
  }
}
