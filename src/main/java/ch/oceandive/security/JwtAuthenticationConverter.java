package ch.oceandive.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Custom JWT authentication converter that extracts roles from the JWT token
 * and converts them into Spring Security GrantedAuthority objects.
 *
 * IMPORTANT: This converter expects clean role names in JWT (e.g., ["ADMIN", "USER"])
 * and adds the ROLE_ prefix for Spring Security compatibility.
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationConverter.class);

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        String username = jwt.getSubject();

        logger.debug("Converting JWT for user '{}' with authorities: {}", username, authorities);
        return new JwtAuthenticationToken(jwt, authorities, username);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        try {
            // Extract roles from JWT token (expects clean format like ["ADMIN", "USER"])
            List<String> roles = jwt.getClaimAsStringList("roles");

            if (roles != null && !roles.isEmpty()) {
                logger.debug("Extracted clean roles from JWT: {}", roles);

                // Convert to Spring Security authorities by adding ROLE_ prefix
                List<GrantedAuthority> authorities = roles.stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role) // Add ROLE_ prefix if missing
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toUnmodifiableList());

                logger.debug("Converted to Spring Security authorities: {}", authorities);
                return authorities;
            }
        } catch (Exception e) {
            logger.error("Error extracting authorities from JWT for user '{}': {}",
                jwt.getSubject(), e.getMessage(), e);
        }

        logger.warn("No roles found in JWT token for user '{}', returning empty authorities list",
            jwt.getSubject());
        return Collections.emptyList();
    }
}