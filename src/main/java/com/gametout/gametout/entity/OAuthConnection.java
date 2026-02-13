package com.gametout.gametout.entity;

import com.gametout.gametout.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a linked OAuth account for a user.
 * Allows users to have multiple OAuth providers linked to their account.
 */
@Entity
@Table(
    name = "oauth_connections",
    indexes = {
        @Index(name = "idx_oauth_provider_user_id", columnList = "provider, providerUserId", unique = true),
        @Index(name = "idx_oauth_user", columnList = "user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "providerUserId"})
    }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuthConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false)
    private String providerUserId;

    @Column(length = 512)
    private String accessToken;

    @Column(length = 512)
    private String refreshToken;

    private LocalDateTime tokenExpiresAt;

    private String providerEmail;

    private String providerUsername;

    private String avatarUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
