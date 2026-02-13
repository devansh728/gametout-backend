package com.gametout.gametout.service;

import com.gametout.gametout.configuration.OAuth2Config;
import com.gametout.gametout.entity.UserAccount;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for JWT token generation and validation.
 * Used for OAuth2 authentication (Discord, LinkedIn, Steam).
 */
@Service
@Slf4j
public class JwtService {

    private final OAuth2Config oauth2Config;
    private final SecretKey secretKey;

    public JwtService(OAuth2Config oauth2Config) {
        this.oauth2Config = oauth2Config;
        // Initialize secret key from config
        String secret = oauth2Config.getJwt().getSecret();
        if (secret == null || secret.length() < 32) {
            // Generate a default key for development (should be configured in production)
            secret = "gametout-oauth2-jwt-secret-key-must-be-at-least-256-bits";
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a JWT token for the given user.
     */
    public String generateToken(UserAccount user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        claims.put("provider", user.getAuthProvider().name());

        Date now = new Date();
        Date expiry = new Date(now.getTime() + oauth2Config.getJwt().getExpiration());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuer(oauth2Config.getJwt().getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validate a JWT token and return the claims.
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extract user ID from token.
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Check if token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * Get expiration time in seconds.
     */
    public Long getExpirationInSeconds() {
        return oauth2Config.getJwt().getExpiration() / 1000;
    }
}
