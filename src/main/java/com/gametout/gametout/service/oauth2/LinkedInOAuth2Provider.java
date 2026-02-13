package com.gametout.gametout.service.oauth2;

import com.gametout.gametout.configuration.OAuth2Config;
import com.gametout.gametout.dto.OAuth2UserInfo;
import com.gametout.gametout.enums.AuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * LinkedIn OAuth2 provider implementation using OpenID Connect.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LinkedInOAuth2Provider implements OAuth2Provider {

    private final OAuth2Config oauth2Config;
    private final WebClient webClient = WebClient.builder().build();

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.LINKEDIN;
    }

    @Override
    public String getAuthorizationUrl(String state) {
        OAuth2Config.LinkedInConfig config = oauth2Config.getLinkedin();
        
        return config.getAuthorizationUri() +
                "?client_id=" + encode(config.getClientId()) +
                "&redirect_uri=" + encode(config.getRedirectUri()) +
                "&response_type=code" +
                "&scope=" + encode(config.getScope()) +
                "&state=" + encode(state);
    }

    @Override
    public OAuth2UserInfo authenticate(String code) throws OAuth2AuthenticationException {
        try {
            OAuth2Config.LinkedInConfig config = oauth2Config.getLinkedin();

            // Exchange code for token
            Map<String, Object> tokenResponse = webClient.post()
                    .uri(config.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                            .with("code", code)
                            .with("redirect_uri", config.getRedirectUri())
                            .with("client_id", config.getClientId())
                            .with("client_secret", config.getClientSecret()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                throw new OAuth2AuthenticationException("Failed to obtain access token from LinkedIn");
            }

            String accessToken = (String) tokenResponse.get("access_token");
            Integer expiresIn = (Integer) tokenResponse.get("expires_in");

            // Fetch user info using OpenID Connect userinfo endpoint
            Map<String, Object> userResponse = webClient.get()
                    .uri(config.getUserInfoUri())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (userResponse == null || !userResponse.containsKey("sub")) {
                throw new OAuth2AuthenticationException("Failed to fetch user info from LinkedIn");
            }

            String id = (String) userResponse.get("sub");
            String email = (String) userResponse.get("email");
            String name = (String) userResponse.get("name");
            String picture = (String) userResponse.get("picture");

            // Use email or name as username
            String username = name != null ? name : email;

            log.info("LinkedIn authentication successful for user: {} ({})", username, id);

            return OAuth2UserInfo.builder()
                    .id(id)
                    .email(email)
                    .username(username)
                    .avatarUrl(picture)
                    .provider(AuthProvider.LINKEDIN)
                    .accessToken(accessToken)
                    .expiresIn(expiresIn != null ? expiresIn.longValue() : null)
                    .build();

        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("LinkedIn authentication failed", e);
            throw new OAuth2AuthenticationException("LinkedIn authentication failed: " + e.getMessage(), e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
