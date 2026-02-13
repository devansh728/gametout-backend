package com.gametout.gametout.service.oauth2;

import com.gametout.gametout.configuration.OAuth2Config;
import com.gametout.gametout.dto.OAuth2UserInfo;
import com.gametout.gametout.enums.AuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Steam OpenID 2.0 provider implementation.
 * Note: Steam uses OpenID 2.0, not OAuth2, so the flow is different.
 * Steam does not provide email addresses.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SteamOAuth2Provider implements OAuth2Provider {

    private final OAuth2Config oauth2Config;
    private final WebClient webClient = WebClient.builder().build();

    // Pattern to extract Steam ID from claimed_id
    private static final Pattern STEAM_ID_PATTERN = Pattern.compile("https://steamcommunity\\.com/openid/id/(\\d+)");

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.STEAM;
    }

    @Override
    public String getAuthorizationUrl(String state) {
        OAuth2Config.SteamConfig config = oauth2Config.getSteam();
        String realm = config.getRealm();
        
        return config.getOpenIdEndpoint() +
                "?openid.ns=" + encode("http://specs.openid.net/auth/2.0") +
                "&openid.mode=checkid_setup" +
                "&openid.return_to=" + encode(config.getRedirectUri() + "?state=" + state) +
                "&openid.realm=" + encode(realm) +
                "&openid.identity=" + encode("http://specs.openid.net/auth/2.0/identifier_select") +
                "&openid.claimed_id=" + encode("http://specs.openid.net/auth/2.0/identifier_select");
    }

    /**
     * For Steam, we need to handle OpenID verification differently.
     * The 'code' parameter here is actually the full query string from the callback.
     */
    @Override
    public OAuth2UserInfo authenticate(String callbackParams) throws OAuth2AuthenticationException {
        try {
            // Parse the callback parameters
            Map<String, String> params = parseQueryString(callbackParams);

            // Verify the OpenID response
            String claimedId = params.get("openid.claimed_id");
            if (claimedId == null) {
                throw new OAuth2AuthenticationException("No claimed_id in Steam callback");
            }

            // Extract Steam ID from claimed_id
            Matcher matcher = STEAM_ID_PATTERN.matcher(claimedId);
            if (!matcher.find()) {
                throw new OAuth2AuthenticationException("Invalid Steam claimed_id format");
            }
            String steamId = matcher.group(1);

            // Verify the signature with Steam
            boolean verified = verifyOpenIdSignature(params);
            if (!verified) {
                throw new OAuth2AuthenticationException("Steam OpenID signature verification failed");
            }

            // Fetch user info from Steam API
            OAuth2UserInfo userInfo = fetchSteamUserInfo(steamId);

            log.info("Steam authentication successful for user: {} ({})", userInfo.getUsername(), steamId);

            return userInfo;

        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Steam authentication failed", e);
            throw new OAuth2AuthenticationException("Steam authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Verify the OpenID signature with Steam.
     */
    private boolean verifyOpenIdSignature(Map<String, String> params) {
        try {
            OAuth2Config.SteamConfig config = oauth2Config.getSteam();

            // Build verification request
            StringBuilder verifyUrl = new StringBuilder(config.getOpenIdEndpoint() + "?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey().startsWith("openid.")) {
                    verifyUrl.append(encode(entry.getKey()))
                            .append("=")
                            .append(encode(entry.getValue()))
                            .append("&");
                }
            }
            // Change mode to check_authentication
            verifyUrl.append("openid.mode=check_authentication");

            String response = webClient.get()
                    .uri(verifyUrl.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return response != null && response.contains("is_valid:true");

        } catch (Exception e) {
            log.error("Failed to verify Steam OpenID signature", e);
            return false;
        }
    }

    /**
     * Fetch user info from Steam Web API.
     */
    private OAuth2UserInfo fetchSteamUserInfo(String steamId) throws OAuth2AuthenticationException {
        try {
            OAuth2Config.SteamConfig config = oauth2Config.getSteam();

            String url = config.getUserInfoUri() +
                    "?key=" + config.getApiKey() +
                    "&steamids=" + steamId;

            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new OAuth2AuthenticationException("Failed to fetch Steam user info");
            }

            Map<String, Object> responseData = (Map<String, Object>) response.get("response");
            if (responseData == null) {
                throw new OAuth2AuthenticationException("Invalid Steam API response");
            }

            List<Map<String, Object>> players = (List<Map<String, Object>>) responseData.get("players");
            if (players == null || players.isEmpty()) {
                throw new OAuth2AuthenticationException("No player data in Steam response");
            }

            Map<String, Object> player = players.get(0);
            String personaName = (String) player.get("personaname");
            String avatarUrl = (String) player.get("avatarfull");

            return OAuth2UserInfo.builder()
                    .id(steamId)
                    .email(null) // Steam doesn't provide email
                    .username(personaName)
                    .avatarUrl(avatarUrl)
                    .provider(AuthProvider.STEAM)
                    .build();

        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new OAuth2AuthenticationException("Failed to fetch Steam user info: " + e.getMessage(), e);
        }
    }

    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new java.util.HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }
        
        // Remove leading '?' if present
        if (queryString.startsWith("?")) {
            queryString = queryString.substring(1);
        }

        for (String param : queryString.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                params.put(
                    java.net.URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                    java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8)
                );
            }
        }
        return params;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
