package com.gametout.gametout.dto;

import com.gametout.gametout.enums.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing user information retrieved from OAuth2 provider.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2UserInfo {
    private String id;
    private String email;
    private String username;
    private String avatarUrl;
    private AuthProvider provider;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}
