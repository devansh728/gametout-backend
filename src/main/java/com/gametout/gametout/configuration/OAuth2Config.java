package com.gametout.gametout.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for OAuth2 providers.
 */
@Configuration
@ConfigurationProperties(prefix = "oauth2")
@Data
public class OAuth2Config {

    private DiscordConfig discord = new DiscordConfig();
    private LinkedInConfig linkedin = new LinkedInConfig();
    private SteamConfig steam = new SteamConfig();
    private JwtConfig jwt = new JwtConfig();

    @Data
    public static class DiscordConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String scope = "identify email";
        private String authorizationUri = "https://discord.com/api/oauth2/authorize";
        private String tokenUri = "https://discord.com/api/oauth2/token";
        private String userInfoUri = "https://discord.com/api/users/@me";
    }

    @Data
    public static class LinkedInConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String scope = "openid profile email";
        private String authorizationUri = "https://www.linkedin.com/oauth/v2/authorization";
        private String tokenUri = "https://www.linkedin.com/oauth/v2/accessToken";
        private String userInfoUri = "https://api.linkedin.com/v2/userinfo";
    }

    @Data
    public static class SteamConfig {
        private String apiKey;
        private String redirectUri;
        private String realm;
        private String openIdEndpoint = "https://steamcommunity.com/openid/login";
        private String userInfoUri = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/";
    }

    @Data
    public static class JwtConfig {
        private String secret;
        private Long expiration = 86400000L; // 24 hours in milliseconds
        private String issuer = "gametout";
    }
}
