package com.gametout.gametout.controller;

import com.gametout.gametout.dto.AuthenticatedUser;
import com.gametout.gametout.dto.SubscriptionDTO;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Get current user's subscription details
     * GET /api/user/subscription
     */
    @GetMapping("/subscription")
    public ResponseEntity<SubscriptionDTO> getSubscription(Authentication auth) {
        UserAccount user = ((AuthenticatedUser) auth.getPrincipal()).getUser();
        SubscriptionDTO subscription = subscriptionService.getSubscription(user.getId());
        return ResponseEntity.ok(subscription);
    }

    /**
     * Check if current user has elite access
     * GET /api/user/subscription/status
     */
    @GetMapping("/subscription/status")
    public ResponseEntity<SubscriptionService.EliteAccessStatus> getEliteStatus(Authentication auth) {
        UserAccount user = ((AuthenticatedUser) auth.getPrincipal()).getUser();
        SubscriptionService.EliteAccessStatus status = subscriptionService.getEliteAccessStatus(user.getId());
        return ResponseEntity.ok(status);
    }

    /**
     * Cancel subscription
     * POST /api/user/subscription/cancel
     */
    @PostMapping("/subscription/cancel")
    public ResponseEntity<Void> cancelSubscription(Authentication auth) {
        UserAccount user = ((AuthenticatedUser) auth.getPrincipal()).getUser();
        subscriptionService.cancelSubscription(user.getId());
        return ResponseEntity.ok().build();
    }
}
