package com.gametout.gametout.dto;

import com.gametout.gametout.enums.SubscriptionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to create a Razorpay order for subscription
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOrderRequest {
    
    @NotNull(message = "Subscription type is required")
    private SubscriptionType subscriptionType;
    
    // Optional: for future use if you want to support multiple durations
    private Integer durationMonths = 12; // Default 1 year
}
