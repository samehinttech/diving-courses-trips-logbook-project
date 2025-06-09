package ch.oceandive.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter to debug authentication issues in JWT processing
 */
@Component
@Profile("dev") // Only active in the development stage for debugging purposes
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,@NonNull  HttpServletResponse response,@NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Only log details for protected endpoints
        if (path.contains("/api/auth/user/profile") || path.contains("/api/auth/admin/profile")) {
            String authHeader = request.getHeader("Authorization");
            logger.debug("Request to protected endpoint: {}", path);
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                logger.debug("Authorization header present");
                
                // Log authentication details if authentication exists
                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    var auth = SecurityContextHolder.getContext().getAuthentication();
                    logger.debug("Authentication: isAuthenticated={}, principal={}, authorities={}",
                            auth.isAuthenticated(),
                            auth.getPrincipal(),
                            auth.getAuthorities());
                } else {
                    logger.warn("No authentication present in SecurityContext for protected endpoint");
                }
            } else {
                logger.warn("No Authorization header for protected endpoint");
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
