package com.gametout.gametout.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gametout.gametout.enums.AuthProvider;
import com.gametout.gametout.enums.SubscriptionType;
import com.gametout.gametout.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(
    name = "user_accounts",
    indexes = {
        @Index(name = "idx_user_firebase_uid", columnList = "firebaseUid"),
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_subscription_expires", columnList = "subscriptionExpiresAt"),
        @Index(name = "idx_user_subscription_type", columnList = "subscriptionType"),
        @Index(name = "idx_user_auth_provider", columnList = "authProvider")
    }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nullable for OAuth-only users (Discord, LinkedIn, Steam)
    @Column(unique = true)
    private String firebaseUid;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.PREMIUM; // USER, PREMIUM, ADMIN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider = AuthProvider.FIREBASE;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Subscription fields (denormalized for fast access)
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type")
    private SubscriptionType subscriptionType = SubscriptionType.VIEWER;  // VIEWER, CREATOR

    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt = LocalDateTime.of(2026, 4, 30, 23, 59);

    // Bidirectional relationship with PortfolioProfile
    @JsonIgnore
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private PortfolioProfile portfolio;

    // Bidirectional relationship with Subscription
    @JsonIgnore
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Subscription subscription;

    // OAuth connections (Discord, LinkedIn, Steam, etc.)
    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OAuthConnection> oauthConnections = new ArrayList<>();

    /**
     * Check if user has an active elite subscription
     */
    public boolean hasEliteAccess() {
        // Admin always has access
        // if (role == UserRole.ADMIN) {
        //     return true;
        // }
        // // Check subscription expiry
        // return subscriptionType != null 
        //     && subscriptionExpiresAt != null 
        //     && subscriptionExpiresAt.isAfter(LocalDateTime.now());
        return true;
    }

    /**
     * Check if user can create premium portfolios
     */
    public boolean canCreatePremiumPortfolio() {
        // if (role == UserRole.ADMIN) {
        //     return true;
        // }
        // return hasEliteAccess() && subscriptionType == SubscriptionType.CREATOR;
        return true;
    }

    /**
     * Check if user has a portfolio
     */
    public boolean hasPortfolio() {
        return portfolio != null;
    }

    /**
     * Check if user authenticated via Firebase (Google/GitHub)
     */
    public boolean isFirebaseUser() {
        return authProvider == AuthProvider.FIREBASE && firebaseUid != null;
    }

    /**
     * Check if user authenticated via OAuth2 (Discord/LinkedIn/Steam)
     */
    public boolean isOAuth2User() {
        return authProvider != AuthProvider.FIREBASE;
    }
}

