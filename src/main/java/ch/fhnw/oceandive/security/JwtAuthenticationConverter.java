package ch.fhnw.oceandive.security;


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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom JWT authentication converter that extracts roles from the JWT token
 * and converts them into Spring Security GrantedAuthority objects.
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationConverter.class);

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        logger.debug("Converting JWT to token with authorities: {}", authorities);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        try {
            List<String> roles = null;
            // Try to get roles as a string list first
            try {
                roles = jwt.getClaimAsStringList("roles");
            } catch (Exception e) {
                // If that fails, try to get as a generic claim and convert
                Object rolesObj = jwt.getClaim("roles");
                if (rolesObj instanceof List) {
                    roles = ((List<?>) rolesObj).stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                }
            }
            
            if (roles != null && !roles.isEmpty()) {
                logger.debug("Extracted roles from token: {}", roles);
                return roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error extracting authorities from JWT: {}", e.getMessage(), e);
        }
        
        logger.warn("No roles found in JWT token, returning empty authorities list");
        return Collections.emptyList();
    }
}
