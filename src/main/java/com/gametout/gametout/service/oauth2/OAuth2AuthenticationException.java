package com.gametout.gametout.service.oauth2;

/**
 * Exception thrown when OAuth2 authentication fails.
 */
public class OAuth2AuthenticationException extends Exception {

    public OAuth2AuthenticationException(String message) {
        super(message);
    }

    public OAuth2AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
