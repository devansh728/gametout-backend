package com.gametout.gametout.controller;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.gametout.gametout.dto.AuthUserResponse;
import com.gametout.gametout.dto.EmailVerificationStatus;
import com.gametout.gametout.service.AuthService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Called after frontend login (Firebase)
     */
    @GetMapping("/me")
    public AuthUserResponse me(Authentication authentication) {
        return authService.currentUser(authentication);
    }

    /**
     * Enforce email verification
     */
    @GetMapping("/verify-status")
    public EmailVerificationStatus emailStatus(Authentication authentication) {
        return authService.emailVerificationStatus(authentication);
    }

    /**
     * Logout (token revocation)
     */
    @PostMapping("/logout")
    public void logout(Authentication authentication) {
        authService.revoke(authentication);
    }
}

