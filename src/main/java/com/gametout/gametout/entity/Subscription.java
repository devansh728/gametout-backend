package com.gametout.gametout.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gametout.gametout.enums.SubscriptionStatus;
import com.gametout.gametout.enums.SubscriptionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "subscriptions",
    indexes = {
        @Index(name = "idx_subscription_user", columnList = "user_id"),
        @Index(name = "idx_subscription_status", columnList = "status"),
        @Index(name = "idx_subscription_expires", columnList = "expires_at")
    }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false)
    private SubscriptionType subscriptionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if the subscription is currently active
     */
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE 
            && expiresAt != null 
            && expiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Check if user has elite access (either viewer or creator)
     */
    public boolean hasEliteAccess() {
        return isActive();
    }

    /**
     * Check if user can create premium portfolios
     */
    public boolean canCreatePremiumPortfolio() {
        return isActive() && subscriptionType == SubscriptionType.CREATOR;
    }
}
