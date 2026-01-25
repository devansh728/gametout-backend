package com.gametout.gametout.enums;

public enum PaymentStatus {
    CREATED,    // Order created, awaiting payment
    AUTHORIZED, // Payment authorized but not captured
    CAPTURED,   // Payment successfully captured
    FAILED,     // Payment failed
    REFUNDED    // Payment was refunded
}
