package ch.oceandive.security;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Debug endpoint to verify JWT token contents.
 * This only enabled in development and will be removed in production.
 */
@RestController
@RequestMapping("/api/debug")
@Profile("dev")
public class JwtDebugEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtDebugEndpoint.class);

    public JwtDebugEndpoint() {
    }

    @GetMapping("/jwt-info")
    public Map<String, Object> getJwtInfo(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("error", "No authentication present");
            return response;
        }

        response.put("authentication_class", authentication.getClass().getName());
        response.put("is_authenticated", authentication.isAuthenticated());
        response.put("authorities", authentication.getAuthorities());
        response.put("principal_class", authentication.getPrincipal().getClass().getName());

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
          Jwt jwt = jwtAuth.getToken();
            
            response.put("jwt_headers", jwt.getHeaders());
            response.put("jwt_claims", jwt.getClaims());
            response.put("jwt_subject", jwt.getSubject());
            response.put("jwt_issuer", jwt.getIssuer());
            response.put("jwt_id", jwt.getId());
            response.put("jwt_expiry", jwt.getExpiresAt());
            
            try {
                response.put("jwt_roles", jwt.getClaimAsStringList("roles"));
            } catch (Exception e) {
                response.put("jwt_roles_error", e.getMessage());
            }
        }

        logger.debug("JWT Debug info: {}", response);
        return response;
    }
}
