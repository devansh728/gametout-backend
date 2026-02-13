package com.gametout.gametout.controller;

import com.gametout.gametout.dto.AuthenticatedUser;
import com.gametout.gametout.dto.LinkedAccountDTO;
import com.gametout.gametout.dto.OAuth2AuthorizationRequest;
import com.gametout.gametout.dto.OAuth2TokenResponse;
import com.gametout.gametout.enums.AuthProvider;
import com.gametout.gametout.service.OAuth2Service;
import com.gametout.gametout.service.oauth2.OAuth2AuthenticationException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Controller for OAuth2 authentication endpoints.
 * Handles Discord, LinkedIn, and Steam authentication.
 */
@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;

    @Value("${app.frontend-url:http://localhost:5000}")
    private String frontendUrl;

    /**
     * Initiate OAuth2 authorization flow.
     * Returns authorization URL for frontend to redirect to.
     */
    @GetMapping("/authorize/{provider}")
    public ResponseEntity<OAuth2AuthorizationRequest> authorize(
            @PathVariable String provider
    ) {
        try {
            AuthProvider authProvider = parseProvider(provider);
            OAuth2AuthorizationRequest request = oauth2Service.getAuthorizationUrl(authProvider, null);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Direct redirect to provider authorization page.
     * Use this for popup-based login.
     */
    @GetMapping("/login/{provider}")
    public void loginRedirect(
            @PathVariable String provider,
            HttpServletResponse response
    ) throws IOException {
        try {
            AuthProvider authProvider = parseProvider(provider);
            OAuth2AuthorizationRequest request = oauth2Service.getAuthorizationUrl(authProvider, null);
            response.sendRedirect(request.getAuthorizationUrl());
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid provider: " + provider);
        }
    }

    /**
     * Handle OAuth2 callback from provider.
     * Redirects to frontend with token or error.
     */
    @GetMapping("/callback/{provider}")
    public void callback(
            @PathVariable String provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false, name = "error_description") String errorDescription,
            HttpServletResponse response
    ) throws IOException {
        String redirectUrl;

        try {
            if (error != null) {
                log.warn("OAuth2 provider returned error: {} - {}", error, errorDescription);
                redirectUrl = buildErrorRedirect("Provider error: " + (errorDescription != null ? errorDescription : error));
            } else if (code == null && provider.equalsIgnoreCase("steam")) {
                // Steam uses OpenID, handle differently
                // For Steam, we need to capture all query params
                AuthProvider authProvider = parseProvider(provider);
                
                // Build callback params string from request
                String callbackParams = buildSteamCallbackParams(state);
                
                OAuth2TokenResponse tokenResponse = oauth2Service.handleCallback(authProvider, callbackParams, state);
                redirectUrl = buildSuccessRedirect(tokenResponse);
            } else if (code != null && state != null) {
                AuthProvider authProvider = parseProvider(provider);
                OAuth2TokenResponse tokenResponse = oauth2Service.handleCallback(authProvider, code, state);
                redirectUrl = buildSuccessRedirect(tokenResponse);
            } else {
                redirectUrl = buildErrorRedirect("Missing authorization code or state");
            }
        } catch (OAuth2AuthenticationException e) {
            log.error("OAuth2 authentication failed: {}", e.getMessage());
            redirectUrl = buildErrorRedirect(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid provider: {}", provider);
            redirectUrl = buildErrorRedirect("Invalid provider: " + provider);
        } catch (Exception e) {
            log.error("OAuth2 callback error", e);
            redirectUrl = buildErrorRedirect("Authentication failed. Please try again.");
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * Handle Steam OpenID callback (special case).
     */
    @GetMapping("/callback/steam")
    public void steamCallback(
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) throws IOException {
        String redirectUrl;

        try {
            String state = params.get("state");
            if (state == null) {
                state = params.get("openid.return_to");
                if (state != null && state.contains("state=")) {
                    state = state.substring(state.indexOf("state=") + 6);
                    if (state.contains("&")) {
                        state = state.substring(0, state.indexOf("&"));
                    }
                }
            }

            if (state == null) {
                redirectUrl = buildErrorRedirect("Missing state parameter");
            } else {
                // Build query string for Steam verification
                StringBuilder queryString = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (queryString.length() > 0) queryString.append("&");
                    queryString.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                              .append("=")
                              .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                }

                OAuth2TokenResponse tokenResponse = oauth2Service.handleCallback(
                        AuthProvider.STEAM,
                        queryString.toString(),
                        state
                );
                redirectUrl = buildSuccessRedirect(tokenResponse);
            }
        } catch (OAuth2AuthenticationException e) {
            log.error("Steam authentication failed: {}", e.getMessage());
            redirectUrl = buildErrorRedirect(e.getMessage());
        } catch (Exception e) {
            log.error("Steam callback error", e);
            redirectUrl = buildErrorRedirect("Steam authentication failed. Please try again.");
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * Link OAuth account to existing user.
     * Requires authentication.
     */
    @GetMapping("/link/{provider}")
    public ResponseEntity<OAuth2AuthorizationRequest> linkAccount(
            @PathVariable String provider,
            Authentication authentication
    ) {
        try {
            AuthProvider authProvider = parseProvider(provider);
            AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
            Long userId = principal.getUser().getId();

            OAuth2AuthorizationRequest request = oauth2Service.linkAccount(authProvider, userId);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Unlink OAuth account from user.
     * Requires authentication.
     */
    @DeleteMapping("/unlink/{provider}")
    public ResponseEntity<Void> unlinkAccount(
            @PathVariable String provider,
            Authentication authentication
    ) {
        try {
            AuthProvider authProvider = parseProvider(provider);
            AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
            Long userId = principal.getUser().getId();

            oauth2Service.unlinkAccount(authProvider, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Get all linked OAuth accounts for current user.
     * Requires authentication.
     */
    @GetMapping("/linked-accounts")
    public ResponseEntity<List<LinkedAccountDTO>> getLinkedAccounts(Authentication authentication) {
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        Long userId = principal.getUser().getId();

        List<LinkedAccountDTO> accounts = oauth2Service.getLinkedAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    // Private helper methods

    private AuthProvider parseProvider(String provider) {
        try {
            return AuthProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }

    private String buildSuccessRedirect(OAuth2TokenResponse tokenResponse) {
        return frontendUrl + "/auth/callback" +
                "?token=" + encode(tokenResponse.getAccessToken()) +
                "&provider=" + encode(tokenResponse.getProvider()) +
                "&userId=" + tokenResponse.getUserId() +
                "&newUser=" + tokenResponse.isNewUser();
    }

    private String buildErrorRedirect(String error) {
        return frontendUrl + "/auth/callback?error=" + encode(error);
    }

    private String buildSteamCallbackParams(String state) {
        // This is a placeholder - actual implementation would capture all OpenID params
        return "state=" + state;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
