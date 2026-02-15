package com.gametout.gametout.service;

import com.gametout.gametout.dto.LinkedAccountDTO;
import com.gametout.gametout.dto.OAuth2AuthorizationRequest;
import com.gametout.gametout.dto.OAuth2TokenResponse;
import com.gametout.gametout.dto.OAuth2UserInfo;
import com.gametout.gametout.entity.OAuthConnection;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.enums.AuthProvider;
import com.gametout.gametout.enums.UserRole;
import com.gametout.gametout.repository.OAuthConnectionRepository;
import com.gametout.gametout.repository.UserAccountRepository;
import com.gametout.gametout.service.oauth2.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Core OAuth2 service that orchestrates authentication with external providers.
 */
@Service
@Transactional
@Slf4j
public class OAuth2Service {

    private static final String STATE_PREFIX = "oauth2:state:";
    private static final Duration STATE_EXPIRY = Duration.ofMinutes(10);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final Map<AuthProvider, OAuth2Provider> providers;
    private final UserAccountRepository userRepository;
    private final OAuthConnectionRepository oauthConnectionRepository;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    public OAuth2Service(
            DiscordOAuth2Provider discordProvider,
            LinkedInOAuth2Provider linkedInProvider,
            SteamOAuth2Provider steamProvider,
            UserAccountRepository userRepository,
            OAuthConnectionRepository oauthConnectionRepository,
            JwtService jwtService,
            StringRedisTemplate redisTemplate
    ) {
        this.providers = new EnumMap<>(AuthProvider.class);
        this.providers.put(AuthProvider.DISCORD, discordProvider);
        this.providers.put(AuthProvider.LINKEDIN, linkedInProvider);
        this.providers.put(AuthProvider.STEAM, steamProvider);

        this.userRepository = userRepository;
        this.oauthConnectionRepository = oauthConnectionRepository;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Generate authorization URL for the given provider.
     */
    public OAuth2AuthorizationRequest getAuthorizationUrl(AuthProvider provider, Long linkToUserId) {
        OAuth2Provider oauthProvider = getProvider(provider);

        // Generate secure state
        String state = generateState();

        // Store state in Redis with optional user ID for linking
        String stateData = linkToUserId != null ? linkToUserId.toString() : "";
        redisTemplate.opsForValue().set(
                STATE_PREFIX + state,
                stateData,
                STATE_EXPIRY.toSeconds(),
                TimeUnit.SECONDS
        );

        String authUrl = oauthProvider.getAuthorizationUrl(state);

        return OAuth2AuthorizationRequest.builder()
                .authorizationUrl(authUrl)
                .state(state)
                .build();
    }

    /**
     * Handle OAuth2 callback and authenticate the user.
     */
    public OAuth2TokenResponse handleCallback(AuthProvider provider, String code, String state)
            throws OAuth2AuthenticationException {
        // Validate state
        String stateKey = STATE_PREFIX + state;
        String stateData = redisTemplate.opsForValue().get(stateKey);
        if (stateData == null) {
            throw new OAuth2AuthenticationException("Invalid or expired state parameter");
        }
        redisTemplate.delete(stateKey);

        // Check if this is a link operation
        Long linkToUserId = stateData.isEmpty() ? null : Long.parseLong(stateData);

        // Authenticate with provider
        OAuth2Provider oauthProvider = getProvider(provider);
        OAuth2UserInfo userInfo = oauthProvider.authenticate(code);

        // Find or create user
        UserAccount user;
        boolean isNewUser = false;

        if (linkToUserId != null) {
            // Link to existing user
            user = userRepository.findById(linkToUserId)
                    .orElseThrow(() -> new OAuth2AuthenticationException("User not found for linking"));

            // Check if already linked to another user
            Optional<OAuthConnection> existingConnection = oauthConnectionRepository
                    .findByProviderAndProviderUserId(provider, userInfo.getId());
            if (existingConnection.isPresent() && !existingConnection.get().getUser().getId().equals(linkToUserId)) {
                throw new OAuth2AuthenticationException("This " + provider.name() + " account is already linked to another user");
            }

            // Create or update connection
            createOrUpdateConnection(user, userInfo);

        } else {
            // Login or register
            Optional<OAuthConnection> existingConnection = oauthConnectionRepository
                    .findByProviderAndProviderUserId(provider, userInfo.getId());

            if (existingConnection.isPresent()) {
                // Existing user
                user = existingConnection.get().getUser();
                // Update connection tokens
                updateConnectionTokens(existingConnection.get(), userInfo);
            } else {
                // Check if email exists (for account merging suggestion)
                if (userInfo.getEmail() != null) {
                    Optional<UserAccount> existingUser = userRepository.findByEmail(userInfo.getEmail());
                    if (existingUser.isPresent()) {
                        // User exists with same email - link the account
                        user = existingUser.get();
                        createOrUpdateConnection(user, userInfo);
                        log.info("Linked {} account to existing user with email: {}", provider, userInfo.getEmail());
                    } else {
                        // New user
                        user = createNewUser(userInfo, provider);
                        isNewUser = true;
                    }
                } else {
                    // No email (Steam) - create new user
                    // Generate a placeholder email
                    String placeholderEmail = provider.name().toLowerCase() + "_" + userInfo.getId() + "@gametout.local";
                    
                    // Check if placeholder already exists
                    Optional<UserAccount> existingUser = userRepository.findByEmail(placeholderEmail);
                    if (existingUser.isPresent()) {
                        user = existingUser.get();
                        createOrUpdateConnection(user, userInfo);
                    } else {
                        userInfo = OAuth2UserInfo.builder()
                                .id(userInfo.getId())
                                .email(placeholderEmail)
                                .username(userInfo.getUsername())
                                .avatarUrl(userInfo.getAvatarUrl())
                                .provider(userInfo.getProvider())
                                .accessToken(userInfo.getAccessToken())
                                .refreshToken(userInfo.getRefreshToken())
                                .expiresIn(userInfo.getExpiresIn())
                                .build();
                        user = createNewUser(userInfo, provider);
                        isNewUser = true;
                    }
                }
            }
        }

        // Generate JWT
        String token = jwtService.generateToken(user);

        return OAuth2TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationInSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .provider(provider.name())
                .newUser(isNewUser)
                .build();
    }

    /**
     * Link an OAuth account to an existing user.
     */
    public OAuth2AuthorizationRequest linkAccount(AuthProvider provider, Long userId) {
        // Check if already linked
        if (oauthConnectionRepository.existsByUserIdAndProvider(userId, provider)) {
            throw new IllegalStateException("Account already linked to " + provider.name());
        }
        return getAuthorizationUrl(provider, userId);
    }

    /**
     * Unlink an OAuth account from a user.
     */
    public void unlinkAccount(AuthProvider provider, Long userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if this is their primary auth method
        if (user.getAuthProvider() == provider) {
            // Check if they have other auth methods
            List<OAuthConnection> connections = oauthConnectionRepository.findByUserId(userId);
            if (connections.size() <= 1 && user.getFirebaseUid() == null) {
                throw new IllegalStateException("Cannot unlink primary authentication method. Link another provider first.");
            }
        }

        oauthConnectionRepository.deleteByUserAndProvider(user, provider);
        log.info("Unlinked {} account from user {}", provider, userId);
    }

    /**
     * Get all linked accounts for a user.
     */
    public List<LinkedAccountDTO> getLinkedAccounts(Long userId) {
        return oauthConnectionRepository.findByUserId(userId).stream()
                .map(conn -> LinkedAccountDTO.builder()
                        .id(conn.getId())
                        .provider(conn.getProvider())
                        .providerUsername(conn.getProviderUsername())
                        .providerEmail(conn.getProviderEmail())
                        .avatarUrl(conn.getAvatarUrl())
                        .linkedAt(conn.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Find user by ID.
     */
    public Optional<UserAccount> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    // Private helper methods

    private OAuth2Provider getProvider(AuthProvider provider) {
        OAuth2Provider oauthProvider = providers.get(provider);
        if (oauthProvider == null) {
            throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
        }
        return oauthProvider;
    }

    private String generateState() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private UserAccount createNewUser(OAuth2UserInfo userInfo, AuthProvider provider) {
        UserAccount user = new UserAccount();
        user.setEmail(userInfo.getEmail());
        user.setEmailVerified(userInfo.getEmail() != null && !userInfo.getEmail().contains("@gametout.local"));
        user.setRole(UserRole.PREMIUM);
        user.setSubscriptionType(com.gametout.gametout.enums.SubscriptionType.VIEWER);
        user.setSubscriptionExpiresAt(java.time.LocalDateTime.now().plusYears(1));
        user.setAuthProvider(provider);
        user.setActive(true);

        user = userRepository.save(user);

        // Create OAuth connection
        createOrUpdateConnection(user, userInfo);

        log.info("Created new user via {}: {} ({})", provider, userInfo.getUsername(), user.getId());

        return user;
    }

    private void createOrUpdateConnection(UserAccount user, OAuth2UserInfo userInfo) {
        OAuthConnection connection = oauthConnectionRepository
                .findByUserAndProvider(user, userInfo.getProvider())
                .orElse(new OAuthConnection());

        connection.setUser(user);
        connection.setProvider(userInfo.getProvider());
        connection.setProviderUserId(userInfo.getId());
        connection.setAccessToken(userInfo.getAccessToken());
        connection.setRefreshToken(userInfo.getRefreshToken());
        connection.setProviderEmail(userInfo.getEmail());
        connection.setProviderUsername(userInfo.getUsername());
        connection.setAvatarUrl(userInfo.getAvatarUrl());

        if (userInfo.getExpiresIn() != null) {
            connection.setTokenExpiresAt(LocalDateTime.now().plusSeconds(userInfo.getExpiresIn()));
        }

        oauthConnectionRepository.save(connection);
    }

    private void updateConnectionTokens(OAuthConnection connection, OAuth2UserInfo userInfo) {
        connection.setAccessToken(userInfo.getAccessToken());
        connection.setRefreshToken(userInfo.getRefreshToken());
        connection.setProviderUsername(userInfo.getUsername());
        connection.setAvatarUrl(userInfo.getAvatarUrl());

        if (userInfo.getExpiresIn() != null) {
            connection.setTokenExpiresAt(LocalDateTime.now().plusSeconds(userInfo.getExpiresIn()));
        }

        oauthConnectionRepository.save(connection);
    }
}
