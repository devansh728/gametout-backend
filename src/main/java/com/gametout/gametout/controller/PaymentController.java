package com.gametout.gametout.controller;

import com.gametout.gametout.dto.*;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create a Razorpay order for subscription purchase
     * POST /api/payment/create-order
     */
    @PostMapping("/create-order")
    public ResponseEntity<PaymentOrderResponse> createOrder(
            Authentication auth,
            @Valid @RequestBody PaymentOrderRequest request) {
        
        UserAccount user = ((AuthenticatedUser) auth.getPrincipal()).getUser();
        PaymentOrderResponse response = paymentService.createOrder(user, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify payment after successful Razorpay checkout
     * POST /api/payment/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<SubscriptionDTO> verifyPayment(
            Authentication auth,
            @Valid @RequestBody PaymentVerifyRequest request) {
        
        UserAccount user = ((AuthenticatedUser) auth.getPrincipal()).getUser();
        SubscriptionDTO subscription = paymentService.verifyPayment(user, request);
        return ResponseEntity.ok(subscription);
    }

    /**
     * Razorpay webhook endpoint
     * POST /api/payment/webhook
     * This endpoint should be public (no auth) as Razorpay will call it
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        
        log.info("Received Razorpay webhook");
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok("OK");
    }
}
