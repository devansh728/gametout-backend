package com.gametout.gametout.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Service to manage token blacklist for logout operations.
 * Supports both OAuth2 JWT tokens and Firebase tokens.
 *
 * Redis structure:
 * - Key: "token:blacklist:{tokenId}"
 * - Value: "{userId}:{authProvider}:{revokedAt}"
 * - TTL: Matches token expiration time
 */
@Service
@Slf4j
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String LOGOUT_TIMESTAMP_PREFIX = "user:logout:";

    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;

    @Value("${app.jwt.expiration:86400}")
    private long jwtExpirationSeconds;

    public TokenBlacklistService(StringRedisTemplate redisTemplate, JwtService jwtService) {
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
    }

    /**
     * Revoke an OAuth2 JWT token by adding it to the blacklist.
     * The token is stored with a TTL matching the token's original expiration.
     */
    public void revokeOAuth2Token(String token, Long userId, String authProvider) {
        try {
            // Extract token ID (or use hash of token if no explicit ID)
            String tokenId = jwtService.getTokenId(token);
            if (tokenId == null) {
                tokenId = hashToken(token);
            }

            String blacklistKey = BLACKLIST_PREFIX + tokenId;
            String blacklistValue = userId + ":" + authProvider + ":" + Instant.now().getEpochSecond();

            // Store in Redis with TTL matching JWT expiration
            redisTemplate.opsForValue().set(
                    blacklistKey,
                    blacklistValue,
                    jwtExpirationSeconds,
                    TimeUnit.SECONDS
            );

            // Also store user-level logout timestamp for additional validation
            String logoutKey = LOGOUT_TIMESTAMP_PREFIX + userId;
            redisTemplate.opsForValue().set(
                    logoutKey,
                    String.valueOf(Instant.now().getEpochSecond()),
                    jwtExpirationSeconds,
                    TimeUnit.SECONDS
            );

            log.info("OAuth2 token revoked for user: {} (provider: {})", userId, authProvider);
        } catch (Exception e) {
            log.error("Failed to revoke OAuth2 token", e);
            throw new RuntimeException("Token revocation failed", e);
        }
    }

    /**
     * Revoke a Firebase token by storing the logout timestamp.
     * Firebase doesn't need individual token tracking; we track when the user logged out.
     */
    public void revokeFirebaseToken(Long userId) {
        try {
            String logoutKey = LOGOUT_TIMESTAMP_PREFIX + userId;
            String logoutTimestamp = String.valueOf(Instant.now().getEpochSecond());

            // Store logout timestamp with long TTL (24 hours)
            redisTemplate.opsForValue().set(
                    logoutKey,
                    logoutTimestamp,
                    Duration.ofHours(24).toSeconds(),
                    TimeUnit.SECONDS
            );

            log.info("Firebase tokens revoked for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to revoke Firebase tokens", e);
            throw new RuntimeException("Token revocation failed", e);
        }
    }

    /**
     * Check if an OAuth2 JWT token has been revoked.
     */
    public boolean isOAuth2TokenRevoked(String token) {
        try {
            String tokenId = jwtService.getTokenId(token);
            if (tokenId == null) {
                tokenId = hashToken(token);
            }

            String blacklistKey = BLACKLIST_PREFIX + tokenId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (Exception e) {
            log.error("Error checking OAuth2 token revocation status", e);
            // Fail closed: if we can't validate, reject the token
            return true;
        }
    }

    /**
     * Check if a Firebase token was issued before the user's logout time.
     * Firebase tokens have iat and auth_time claims.
     */
    public boolean isFirebaseTokenValid(Long userId, Long tokenIssuedAt) {
        try {
            String logoutKey = LOGOUT_TIMESTAMP_PREFIX + userId;
            String logoutTimestamp = redisTemplate.opsForValue().get(logoutKey);

            if (logoutTimestamp == null) {
                // No logout record; token is valid
                return true;
            }

            long logoutTime = Long.parseLong(logoutTimestamp);
            return tokenIssuedAt > logoutTime;
        } catch (Exception e) {
            log.error("Error checking Firebase token validity", e);
            // Fail closed: if we can't validate, reject the token
            return false;
        }
    }

    /**
     * Get user's last logout timestamp (for validation purposes).
     */
    public Long getLastLogoutTime(Long userId) {
        try {
            String logoutKey = LOGOUT_TIMESTAMP_PREFIX + userId;
            String logoutTimestamp = redisTemplate.opsForValue().get(logoutKey);

            if (logoutTimestamp == null) {
                return null;
            }

            return Long.parseLong(logoutTimestamp);
        } catch (Exception e) {
            log.error("Error retrieving logout timestamp", e);
            return null;
        }
    }

    /**
     * Simple hash function for token (SHA-256 first 16 chars).
     */
    private String hashToken(String token) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes());
            // Convert to hex and take first 16 chars
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
                if (sb.length() >= 16) break;
            }
            return sb.toString();
        } catch (Exception e) {
            // Fallback to token substring hash
            return "token_" + Math.abs(token.hashCode());
        }
    }
}
