package com.gametout.gametout.filter;

import com.gametout.gametout.dto.AuthenticatedUser;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.service.FirebaseTokenService;
import com.gametout.gametout.service.UserProvisioningService;
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

    public FirebaseAuthenticationFilter(
            FirebaseTokenService tokenService,
            UserProvisioningService provisioningService
    ) {
        this.tokenService = tokenService;
        this.provisioningService = provisioningService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                FirebaseToken decoded = tokenService.verify(token);
                UserAccount user = provisioningService.getOrCreateUser(decoded);

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

