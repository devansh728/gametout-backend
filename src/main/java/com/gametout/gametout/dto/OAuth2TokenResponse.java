package com.gametout.gametout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing JWT token after successful OAuth2 authentication.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2TokenResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String email;
    private String role;
    private String provider;
    private boolean newUser;
}
