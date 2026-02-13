package com.gametout.gametout.repository;

import com.gametout.gametout.entity.OAuthConnection;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthConnectionRepository extends JpaRepository<OAuthConnection, Long> {

    /**
     * Find OAuth connection by provider and provider user ID
     */
    Optional<OAuthConnection> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    /**
     * Find all OAuth connections for a user
     */
    List<OAuthConnection> findByUser(UserAccount user);

    /**
     * Find all OAuth connections for a user by user ID
     */
    List<OAuthConnection> findByUserId(Long userId);

    /**
     * Find OAuth connection by user and provider
     */
    Optional<OAuthConnection> findByUserAndProvider(UserAccount user, AuthProvider provider);

    /**
     * Find OAuth connection by user ID and provider
     */
    Optional<OAuthConnection> findByUserIdAndProvider(Long userId, AuthProvider provider);

    /**
     * Check if a user has a specific provider linked
     */
    boolean existsByUserAndProvider(UserAccount user, AuthProvider provider);

    /**
     * Check if a user has a specific provider linked by user ID
     */
    boolean existsByUserIdAndProvider(Long userId, AuthProvider provider);

    /**
     * Check if a provider user ID already exists
     */
    boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    /**
     * Delete OAuth connection by user and provider
     */
    void deleteByUserAndProvider(UserAccount user, AuthProvider provider);
}
