package com.gametout.gametout.service.oauth2;

import com.gametout.gametout.dto.OAuth2UserInfo;
import com.gametout.gametout.enums.AuthProvider;

/**
 * Interface for OAuth2 provider implementations.
 */
public interface OAuth2Provider {

    /**
     * Get the provider type.
     */
    AuthProvider getProvider();

    /**
     * Generate the authorization URL.
     */
    String getAuthorizationUrl(String state);

    /**
     * Exchange authorization code for access token and fetch user info.
     */
    OAuth2UserInfo authenticate(String code) throws OAuth2AuthenticationException;

    /**
     * Validate the state parameter.
     */
    default boolean validateState(String state, String expectedState) {
        return state != null && state.equals(expectedState);
    }
}
