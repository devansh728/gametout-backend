package com.gametout.gametout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response after creating a Razorpay order
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOrderResponse {
    
    private String orderId;
    private Integer amount; // in paise
    private String currency;
    private String keyId; // Razorpay public key for frontend
    private String receipt;
    private String description;
    
    // User info for prefill
    private String userEmail;
    private String userName;
}
