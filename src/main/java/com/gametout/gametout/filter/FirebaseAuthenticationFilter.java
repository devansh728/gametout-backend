package com.gametout.gametout.filter;

import com.gametout.gametout.dto.AuthenticatedUser;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.service.FirebaseTokenService;
import com.gametout.gametout.service.UserProvisioningService;
import com.gametout.gametout.service.TokenBlacklistService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseTokenService tokenService;
    private final UserProvisioningService provisioningService;
    private final TokenBlacklistService tokenBlacklistService;

    public FirebaseAuthenticationFilter(
            FirebaseTokenService tokenService,
            UserProvisioningService provisioningService,
            TokenBlacklistService tokenBlacklistService
    ) {
        this.tokenService = tokenService;
        this.provisioningService = provisioningService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip if already authenticated (by OAuth2 filter)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                FirebaseToken decoded = tokenService.verify(token);
                UserAccount user = provisioningService.getOrCreateUser(decoded);

                // Check if token was issued AFTER user's last logout
                // Extract iat claim (issued at) from the token claims
                Object iatObj = decoded.getClaims().getOrDefault("iat", 0L);
                Long tokenIssuedAtSeconds = 0L;
                if (iatObj instanceof Number) {
                    tokenIssuedAtSeconds = ((Number) iatObj).longValue();
                }
                
                if (!tokenBlacklistService.isFirebaseTokenValid(user.getId(), tokenIssuedAtSeconds)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                AuthenticatedUser principal = new AuthenticatedUser(user);

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()
                    );

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (FirebaseAuthException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

