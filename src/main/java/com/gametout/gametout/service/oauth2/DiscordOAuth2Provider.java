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
 * Discord OAuth2 provider implementation.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DiscordOAuth2Provider implements OAuth2Provider {

    private final OAuth2Config oauth2Config;
    private final WebClient webClient = WebClient.builder().build();

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.DISCORD;
    }

    @Override
    public String getAuthorizationUrl(String state) {
        OAuth2Config.DiscordConfig config = oauth2Config.getDiscord();
        
        return config.getAuthorizationUri() +
                "?client_id=" + encode(config.getClientId()) +
                "&redirect_uri=" + encode(config.getRedirectUri()) +
                "&response_type=code" +
                "&scope=" + encode(config.getScope()) +
                "&state=" + encode(state) +
                "&prompt=consent";
    }

    @Override
    public OAuth2UserInfo authenticate(String code) throws OAuth2AuthenticationException {
        try {
            OAuth2Config.DiscordConfig config = oauth2Config.getDiscord();

            // Exchange code for token
            Map<String, Object> tokenResponse = webClient.post()
                    .uri(config.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("client_id", config.getClientId())
                            .with("client_secret", config.getClientSecret())
                            .with("grant_type", "authorization_code")
                            .with("code", code)
                            .with("redirect_uri", config.getRedirectUri()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                throw new OAuth2AuthenticationException("Failed to obtain access token from Discord");
            }

            String accessToken = (String) tokenResponse.get("access_token");
            String refreshToken = (String) tokenResponse.get("refresh_token");
            Integer expiresIn = (Integer) tokenResponse.get("expires_in");

            // Fetch user info
            Map<String, Object> userResponse = webClient.get()
                    .uri(config.getUserInfoUri())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (userResponse == null || !userResponse.containsKey("id")) {
                throw new OAuth2AuthenticationException("Failed to fetch user info from Discord");
            }

            String id = (String) userResponse.get("id");
            String username = (String) userResponse.get("username");
            String email = (String) userResponse.get("email");
            String avatar = (String) userResponse.get("avatar");

            // Build avatar URL
            String avatarUrl = null;
            if (avatar != null) {
                avatarUrl = "https://cdn.discordapp.com/avatars/" + id + "/" + avatar + ".png";
            }

            log.info("Discord authentication successful for user: {} ({})", username, id);

            return OAuth2UserInfo.builder()
                    .id(id)
                    .email(email)
                    .username(username)
                    .avatarUrl(avatarUrl)
                    .provider(AuthProvider.DISCORD)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(expiresIn != null ? expiresIn.longValue() : null)
                    .build();

        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Discord authentication failed", e);
            throw new OAuth2AuthenticationException("Discord authentication failed: " + e.getMessage(), e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
