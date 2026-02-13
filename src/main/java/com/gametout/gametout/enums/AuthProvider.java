package com.gametout.gametout.enums;

/**
 * Enum representing the authentication providers supported by the application.
 * FIREBASE: Google, GitHub (via Firebase)
 * DISCORD, LINKEDIN, STEAM: Direct OAuth2/OpenID
 */
public enum AuthProvider {
    FIREBASE,
    DISCORD,
    LINKEDIN,
    STEAM
}
