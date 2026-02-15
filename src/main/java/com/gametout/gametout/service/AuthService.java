package com.gametout.gametout.service;
import com.gametout.gametout.dto.AuthUserResponse;
import com.gametout.gametout.dto.EmailVerificationStatus;
import com.gametout.gametout.dto.AuthenticatedUser;
import com.gametout.gametout.entity.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;



@Service
@Transactional
@Slf4j
public class AuthService {

    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // @Cacheable(value = "user_profile", key = "#auth.principal.user.id", unless = "#result == null")
    public AuthUserResponse currentUser(Authentication auth) {
        AuthenticatedUser principal = (AuthenticatedUser) auth.getPrincipal();
        UserAccount user = principal.getUser();

        log.debug("Current user ID: {}", user.getId());

        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEmailVerified(),
                user.isActive(),
                user.getSubscriptionType(),
                user.getAuthProvider()
        );
    }

    public EmailVerificationStatus emailVerificationStatus(Authentication auth) {
        AuthenticatedUser principal = (AuthenticatedUser) auth.getPrincipal();
        return new EmailVerificationStatus(
                principal.getUser().isEmailVerified()
        );
    }

    /**
     * Revoke all tokens for the current user (both Firebase and OAuth2).
     * Called on logout to invalidate all existing tokens.
     */
    public void revoke(Authentication auth) {
        AuthenticatedUser principal = (AuthenticatedUser) auth.getPrincipal();
        UserAccount user = principal.getUser();
        Long userId = user.getId();

        // Revoke Firebase tokens if user authenticated via Firebase
        if (user.getFirebaseUid() != null) {
            try {
                FirebaseAuth.getInstance()
                    .revokeRefreshTokens(user.getFirebaseUid());
                log.info("Firebase tokens revoked for user: {}", userId);
            } catch (FirebaseAuthException e) {
                log.error("Failed to revoke Firebase tokens for user: {}", userId, e);
                // Don't throw - continue to OAuth2 revocation
            }
        }

        // Revoke OAuth2 tokens if user authenticated via OAuth2
        if (user.getAuthProvider() != null && !user.getAuthProvider().name().equals("FIREBASE")) {
            tokenBlacklistService.revokeFirebaseToken(userId); // Uses logout timestamp approach
            log.info("OAuth2 tokens revoked for user: {} (provider: {})", userId, user.getAuthProvider());
        }
    }
}

