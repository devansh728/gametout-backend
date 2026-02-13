package com.gametout.gametout.filter;

import com.gametout.gametout.dto.AuthenticatedUser;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.service.JwtService;
import com.gametout.gametout.service.OAuth2Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Filter to authenticate requests using custom JWT tokens.
 * This handles OAuth2 authentication (Discord, LinkedIn, Steam).
 * Works alongside FirebaseAuthenticationFilter.
 */
@Component
@Slf4j
public class OAuth2AuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final OAuth2Service oauth2Service;

    public OAuth2AuthenticationFilter(JwtService jwtService, OAuth2Service oauth2Service) {
        this.jwtService = jwtService;
        this.oauth2Service = oauth2Service;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip if already authenticated (by Firebase filter)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // Check if this is a custom JWT (not Firebase)
            // Firebase tokens are much longer and have a different structure
            if (isCustomJwt(token)) {
                try {
                    Claims claims = jwtService.validateToken(token);
                    Long userId = claims.get("userId", Long.class);

                    if (userId != null) {
                        Optional<UserAccount> userOpt = oauth2Service.findUserById(userId);

                        if (userOpt.isPresent()) {
                            UserAccount user = userOpt.get();

                            if (user.isActive()) {
                                AuthenticatedUser principal = new AuthenticatedUser(user);

                                UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(
                                                principal, null, principal.getAuthorities()
                                        );

                                SecurityContextHolder.getContext().setAuthentication(auth);
                                log.debug("OAuth2 JWT authentication successful for user: {}", userId);
                            } else {
                                log.warn("OAuth2 JWT token for inactive user: {}", userId);
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                return;
                            }
                        } else {
                            log.warn("OAuth2 JWT token for non-existent user: {}", userId);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            return;
                        }
                    }

                } catch (JwtException ex) {
                    log.debug("OAuth2 JWT validation failed: {}", ex.getMessage());
                    // Don't return error - let Firebase filter try
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the token is a custom JWT (vs Firebase token).
     * Firebase tokens have a specific format and are typically longer.
     */
    private boolean isCustomJwt(String token) {
        try {
            // Try to validate with our JWT service
            // If it works, it's our custom JWT
            Claims claims = jwtService.validateToken(token);
            return claims.getIssuer() != null && claims.getIssuer().equals("gametout");
        } catch (JwtException e) {
            // Not a valid custom JWT
            return false;
        }
    }
}
