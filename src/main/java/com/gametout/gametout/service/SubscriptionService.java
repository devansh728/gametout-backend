package com.gametout.gametout.service;

import com.gametout.gametout.dto.SubscriptionDTO;
import com.gametout.gametout.entity.Subscription;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.enums.SubscriptionStatus;
import com.gametout.gametout.enums.UserRole;
import com.gametout.gametout.repository.SubscriptionRepository;
import com.gametout.gametout.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserAccountRepository userRepository;

    /**
     * Get user's current subscription status
     */
    @Cacheable(value = "subscription", key = "#userId", unless = "#result == null")
    public SubscriptionDTO getSubscription(Long userId) {
        return subscriptionRepository
            .findByUserId(userId)
            .map(SubscriptionDTO::fromEntity)
            .orElse(SubscriptionDTO.noSubscription());
    }

    /**
     * Check if user has elite access (can view full profiles)
     */
    @Cacheable(value = "elite_access", key = "#userId")
    public boolean hasEliteAccess(Long userId) {
        // Check user role first (admin always has access)
        UserAccount user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        if (user.getRole() == UserRole.ADMIN) return true;

        // Check subscription
        return user.hasEliteAccess();
    }

    /**
     * Check if user can create premium portfolio
     */
    public boolean canCreatePremiumPortfolio(Long userId) {
        UserAccount user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        return user.canCreatePremiumPortfolio();
    }

    /**
     * Get subscription status for checking access
     */
    public record EliteAccessStatus(
        boolean hasAccess,
        boolean canViewFullProfiles,
        boolean canCreatePremiumPortfolio,
        String subscriptionType,
        Long daysRemaining
    ) {}

    /**
     * Get detailed elite access status
     */
    @Cacheable(value = "elite_status", key = "#userId")
    public EliteAccessStatus getEliteAccessStatus(Long userId) {
        UserAccount user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new EliteAccessStatus(false, false, false, null, 0L);
        }

        // Admin always has full access
        if (user.getRole() == UserRole.ADMIN) {
            return new EliteAccessStatus(true, true, true, "ADMIN", -1L);
        }

        boolean hasAccess = user.hasEliteAccess();
        String type = user.getSubscriptionType() != null ? user.getSubscriptionType().name() : null;
        
        long daysRemaining = 0;
        if (user.getSubscriptionExpiresAt() != null && hasAccess) {
            daysRemaining = java.time.Duration.between(
                LocalDateTime.now(),
                user.getSubscriptionExpiresAt()
            ).toDays();
            if (daysRemaining < 0) daysRemaining = 0;
        }

        return new EliteAccessStatus(
            hasAccess,
            hasAccess, // Viewer benefits
            user.canCreatePremiumPortfolio(),
            type,
            daysRemaining
        );
    }

    /**
     * Expire old subscriptions (runs daily at midnight)
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    @CacheEvict(value = {"subscription", "elite_access", "elite_status", "user_profile"}, allEntries = true)
    public void expireOldSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expiredSubscriptions = subscriptionRepository.findExpiredSubscriptions(now);

        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);

            // Update user
            UserAccount user = subscription.getUser();
            user.setRole(UserRole.USER);
            // Keep subscription type for reference, but it's now expired
            userRepository.save(user);

            log.info("Expired subscription for user {}", user.getId());
        }

        if (!expiredSubscriptions.isEmpty()) {
            log.info("Expired {} subscriptions", expiredSubscriptions.size());
        }
    }

    /**
     * Cancel a subscription
     */
    @Transactional
    @CacheEvict(value = {"subscription", "elite_access", "elite_status", "user_profile"}, key = "#userId")
    public void cancelSubscription(Long userId) {
        Subscription subscription = subscriptionRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("No subscription found"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);

        UserAccount user = subscription.getUser();
        user.setRole(UserRole.USER);
        user.setSubscriptionType(null);
        user.setSubscriptionExpiresAt(null);
        userRepository.save(user);

        log.info("Cancelled subscription for user {}", userId);
    }
}
