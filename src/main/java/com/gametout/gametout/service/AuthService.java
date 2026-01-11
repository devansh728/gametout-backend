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

    @Cacheable(value = "user_profile", key = "#auth.principal.user.id", unless = "#result == null")
    public AuthUserResponse currentUser(Authentication auth) {
        AuthenticatedUser principal = (AuthenticatedUser) auth.getPrincipal();
        UserAccount user = principal.getUser();

        log.info("Current user: {}", user);

        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEmailVerified(),
                user.isActive()
        );
    }

    public EmailVerificationStatus emailVerificationStatus(Authentication auth) {
        AuthenticatedUser principal = (AuthenticatedUser) auth.getPrincipal();
        return new EmailVerificationStatus(
                principal.getUser().isEmailVerified()
        );
    }

    /**
     * Firebase logout = token revocation
     */
    public void revoke(Authentication auth) {
        AuthenticatedUser principal = (AuthenticatedUser) auth.getPrincipal();

        try {
            FirebaseAuth.getInstance()
                .revokeRefreshTokens(principal.getUser().getFirebaseUid());
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Failed to revoke tokens", e);
        }
    }
}

