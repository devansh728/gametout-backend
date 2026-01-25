package com.gametout.gametout.dto;

import com.gametout.gametout.enums.SubscriptionStatus;
import com.gametout.gametout.enums.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for subscription information
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionDTO {
    
    private Long id;
    private SubscriptionType type;
    private SubscriptionStatus status;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private boolean autoRenew;
    private boolean isActive;
    private boolean canViewFullProfiles;
    private boolean canCreatePremiumPortfolio;
    private long daysRemaining;
    
    /**
     * Create DTO from entity
     */
    public static SubscriptionDTO fromEntity(com.gametout.gametout.entity.Subscription subscription) {
        if (subscription == null) {
            return SubscriptionDTO.builder()
                .isActive(false)
                .canViewFullProfiles(false)
                .canCreatePremiumPortfolio(false)
                .daysRemaining(0)
                .build();
        }
        
        long daysRemaining = 0;
        if (subscription.getExpiresAt() != null) {
            daysRemaining = java.time.Duration.between(
                LocalDateTime.now(), 
                subscription.getExpiresAt()
            ).toDays();
            if (daysRemaining < 0) daysRemaining = 0;
        }
        
        return SubscriptionDTO.builder()
            .id(subscription.getId())
            .type(subscription.getSubscriptionType())
            .status(subscription.getStatus())
            .startsAt(subscription.getStartsAt())
            .expiresAt(subscription.getExpiresAt())
            .autoRenew(subscription.isAutoRenew())
            .isActive(subscription.isActive())
            .canViewFullProfiles(subscription.hasEliteAccess())
            .canCreatePremiumPortfolio(subscription.canCreatePremiumPortfolio())
            .daysRemaining(daysRemaining)
            .build();
    }
    
    /**
     * Create a "no subscription" DTO
     */
    public static SubscriptionDTO noSubscription() {
        return SubscriptionDTO.builder()
            .isActive(false)
            .canViewFullProfiles(false)
            .canCreatePremiumPortfolio(false)
            .daysRemaining(0)
            .build();
    }
}
