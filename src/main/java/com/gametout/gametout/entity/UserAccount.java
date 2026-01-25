package com.gametout.gametout.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gametout.gametout.enums.SubscriptionType;
import com.gametout.gametout.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Entity
@Table(
    name = "user_accounts",
    indexes = {
        @Index(name = "idx_user_firebase_uid", columnList = "firebaseUid", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_subscription_expires", columnList = "subscriptionExpiresAt"),
        @Index(name = "idx_user_subscription_type", columnList = "subscriptionType")
    }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String firebaseUid;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // USER, PREMIUM, ADMIN

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Subscription fields (denormalized for fast access)
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type")
    private SubscriptionType subscriptionType;

    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;

    // Bidirectional relationship with PortfolioProfile
    @JsonIgnore
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private PortfolioProfile portfolio;

    // Bidirectional relationship with Subscription
    @JsonIgnore
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Subscription subscription;

    /**
     * Check if user has an active elite subscription
     */
    public boolean hasEliteAccess() {
        // Admin always has access
        if (role == UserRole.ADMIN) {
            return true;
        }
        // Check subscription expiry
        return subscriptionType != null 
            && subscriptionExpiresAt != null 
            && subscriptionExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Check if user can create premium portfolios
     */
    public boolean canCreatePremiumPortfolio() {
        if (role == UserRole.ADMIN) {
            return true;
        }
        return hasEliteAccess() && subscriptionType == SubscriptionType.CREATOR;
    }

    /**
     * Check if user has a portfolio
     */
    public boolean hasPortfolio() {
        return portfolio != null;
    }
}

