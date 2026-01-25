package com.gametout.gametout.service;

import com.gametout.gametout.config.RazorpayConfig;
import com.gametout.gametout.dto.*;
import com.gametout.gametout.entity.PaymentTransaction;
import com.gametout.gametout.entity.Subscription;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.enums.PaymentStatus;
import com.gametout.gametout.enums.SubscriptionStatus;
import com.gametout.gametout.enums.UserRole;
import com.gametout.gametout.repository.PaymentTransactionRepository;
import com.gametout.gametout.repository.SubscriptionRepository;
import com.gametout.gametout.repository.UserAccountRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;
    private final PaymentTransactionRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserAccountRepository userRepository;

    /**
     * Create a Razorpay order for subscription purchase
     */
    @Transactional
    public PaymentOrderResponse createOrder(UserAccount user, PaymentOrderRequest request) {
        try {
            RazorpayConfig.Plan plan = razorpayConfig.getPlan(request.getSubscriptionType());
            
            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", plan.getAmount());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "rcpt_" + user.getId() + "_" + System.currentTimeMillis());
            orderRequest.put("notes", new JSONObject()
                .put("user_id", user.getId())
                .put("plan_type", request.getSubscriptionType().name())
                .put("user_email", user.getEmail())
            );

            Order order = razorpayClient.orders.create(orderRequest);
            String orderId = order.get("id");

            // Save payment transaction
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setUser(user);
            transaction.setRazorpayOrderId(orderId);
            transaction.setAmountPaise(plan.getAmount());
            transaction.setCurrency("INR");
            transaction.setStatus(PaymentStatus.CREATED);
            transaction.setPlanType(request.getSubscriptionType());
            transaction.setPlanDurationMonths(plan.getDurationMonths());
            transaction.setDescription(plan.getDescription());
            transaction.setReceipt(order.get("receipt"));
            
            paymentRepository.save(transaction);

            log.info("Created Razorpay order {} for user {}", orderId, user.getId());

            // Return response for frontend
            return new PaymentOrderResponse(
                orderId,
                plan.getAmount(),
                "INR",
                razorpayConfig.getKeyId(),
                transaction.getReceipt(),
                plan.getDescription(),
                user.getEmail(),
                user.getEmail().split("@")[0] // Simple name extraction
            );

        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order for user {}", user.getId(), e);
            throw new RuntimeException("Failed to create payment order: " + e.getMessage(), e);
        }
    }

    /**
     * Verify payment and activate subscription
     */
    @Transactional
    @CacheEvict(value = {"subscription", "user_profile", "user_entity"}, key = "#user.id")
    public SubscriptionDTO verifyPayment(UserAccount user, PaymentVerifyRequest request) {
        // Find the payment transaction
        PaymentTransaction transaction = paymentRepository
            .findByRazorpayOrderId(request.getRazorpayOrderId())
            .orElseThrow(() -> new RuntimeException("Payment order not found"));

        // Verify it belongs to the user
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Payment order does not belong to this user");
        }

        // Verify signature
        try {
            // Razorpay signature verification
            // The signature is created using: order_id + "|" + payment_id
            String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            
            boolean isValid = Utils.verifySignature(
                payload,
                request.getRazorpaySignature(),
                razorpayConfig.getKeySecret()
            );

            if (!isValid) {
                transaction.setStatus(PaymentStatus.FAILED);
                transaction.setErrorMessage("Invalid payment signature");
                paymentRepository.save(transaction);
                throw new RuntimeException("Payment verification failed: Invalid signature");
            }
        } catch (RazorpayException e) {
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            paymentRepository.save(transaction);
            throw new RuntimeException("Payment verification failed: " + e.getMessage(), e);
        }

        // Update transaction
        transaction.setRazorpayPaymentId(request.getRazorpayPaymentId());
        transaction.setRazorpaySignature(request.getRazorpaySignature());
        transaction.setStatus(PaymentStatus.CAPTURED);
        transaction.setPaidAt(LocalDateTime.now());

        // Create or update subscription
        Subscription subscription = subscriptionRepository
            .findByUserId(user.getId())
            .orElse(new Subscription());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt;
        
        // If already has active subscription, extend from current expiry
        if (subscription.isActive()) {
            expiresAt = subscription.getExpiresAt()
                .plusMonths(transaction.getPlanDurationMonths());
        } else {
            expiresAt = now.plusMonths(transaction.getPlanDurationMonths());
        }

        subscription.setUser(user);
        subscription.setSubscriptionType(transaction.getPlanType());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartsAt(now);
        subscription.setExpiresAt(expiresAt);
        
        subscription = subscriptionRepository.save(subscription);
        transaction.setSubscription(subscription);
        paymentRepository.save(transaction);

        // Update user account
        user.setSubscriptionType(transaction.getPlanType());
        user.setSubscriptionExpiresAt(expiresAt);
        user.setRole(UserRole.PREMIUM);
        userRepository.save(user);

        log.info("Payment verified and subscription activated for user {}", user.getId());

        return SubscriptionDTO.fromEntity(subscription);
    }

    /**
     * Handle Razorpay webhook events
     */
    @Transactional
    public void handleWebhook(String payload, String signature) {
        try {
            // Verify webhook signature
            boolean isValid = Utils.verifyWebhookSignature(
                payload, 
                signature, 
                razorpayConfig.getWebhookSecret()
            );

            if (!isValid) {
                log.warn("Invalid webhook signature");
                return;
            }

            JSONObject event = new JSONObject(payload);
            String eventType = event.getString("event");

            log.info("Received Razorpay webhook: {}", eventType);

            switch (eventType) {
                case "payment.captured" -> handlePaymentCaptured(event);
                case "payment.failed" -> handlePaymentFailed(event);
                case "refund.created" -> handleRefund(event);
                default -> log.debug("Unhandled webhook event: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Error processing webhook", e);
        }
    }

    private void handlePaymentCaptured(JSONObject event) {
        JSONObject payment = event.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
        String orderId = payment.getString("order_id");
        String paymentId = payment.getString("id");

        paymentRepository.findByRazorpayOrderId(orderId).ifPresent(transaction -> {
            if (transaction.getStatus() != PaymentStatus.CAPTURED) {
                transaction.setRazorpayPaymentId(paymentId);
                transaction.setStatus(PaymentStatus.CAPTURED);
                transaction.setPaidAt(LocalDateTime.now());
                paymentRepository.save(transaction);
                log.info("Payment captured via webhook: order={}, payment={}", orderId, paymentId);
            }
        });
    }

    private void handlePaymentFailed(JSONObject event) {
        JSONObject payment = event.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
        String orderId = payment.getString("order_id");
        String errorReason = payment.optJSONObject("error_reason") != null 
            ? payment.getString("error_reason") 
            : "Payment failed";

        paymentRepository.findByRazorpayOrderId(orderId).ifPresent(transaction -> {
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setErrorMessage(errorReason);
            paymentRepository.save(transaction);
            log.info("Payment failed via webhook: order={}", orderId);
        });
    }

    private void handleRefund(JSONObject event) {
        JSONObject refund = event.getJSONObject("payload").getJSONObject("refund").getJSONObject("entity");
        String paymentId = refund.getString("payment_id");

        paymentRepository.findByRazorpayPaymentId(paymentId).ifPresent(transaction -> {
            transaction.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(transaction);
            
            // Cancel subscription
            if (transaction.getSubscription() != null) {
                Subscription subscription = transaction.getSubscription();
                subscription.setStatus(SubscriptionStatus.CANCELLED);
                subscriptionRepository.save(subscription);
                
                // Update user
                UserAccount user = transaction.getUser();
                user.setSubscriptionType(null);
                user.setSubscriptionExpiresAt(null);
                user.setRole(UserRole.USER);
                userRepository.save(user);
            }
            
            log.info("Refund processed: payment={}", paymentId);
        });
    }
}
