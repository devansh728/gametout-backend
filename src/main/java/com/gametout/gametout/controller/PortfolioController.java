package com.gametout.gametout.controller;

import com.gametout.gametout.dto.PortfolioRequest;
import com.gametout.gametout.dto.PortfolioResponseDTO;
import com.gametout.gametout.entity.PortfolioProfile;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.enums.JobCategory;
import com.gametout.gametout.dto.AuthenticatedUser;
import com.gametout.gametout.dto.PortfolioPageResponse;
import com.gametout.gametout.service.PortfolioService;
import com.gametout.gametout.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.Cacheable;

import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService service;
    private final SubscriptionService subscriptionService;

    /**
     * Get total count of portfolios
     * GET /api/portfolio/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCount() {
        long count = service.getTotalCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/list/all")
    @Cacheable(value = "portfolio:list:all", key = "#pageable.pageNumber")
    public PortfolioPageResponse listAll(Pageable pageable) {
        return service.listAll(pageable);
    }

    @PostMapping
    public PortfolioProfile createOrUpdate(
            Authentication auth,
            @RequestBody PortfolioRequest req) {
        UserAccount user = ((AuthenticatedUser) auth.getPrincipal()).getUser();
        return service.createOrUpdate(user, req);
    }

    /**
     * Get current user's own portfolio for editing
     * GET /api/portfolio/my
     */
    @GetMapping("/my")
    public ResponseEntity<PortfolioResponseDTO> getMyPortfolio(Authentication auth) {
        UserAccount user = ((AuthenticatedUser) auth.getPrincipal()).getUser();
        return service.getMyPortfolio(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Check if current user owns a portfolio
     * GET /api/portfolio/{id}/is-owner
     */
    @GetMapping("/{id}/is-owner")
    public ResponseEntity<Map<String, Boolean>> isOwner(
            @PathVariable Long id,
            Authentication auth) {
        UserAccount user = ((AuthenticatedUser) auth.getPrincipal()).getUser();
        boolean isOwner = service.isOwner(id, user.getId());
        return ResponseEntity.ok(Map.of("isOwner", isOwner));
    }

    /**
     * Check if current user can view full profiles (has elite access)
     * GET /api/portfolio/can-view-full
     */
    @GetMapping("/can-view-full")
    public ResponseEntity<SubscriptionService.EliteAccessStatus> canViewFullProfiles(Authentication auth) {
        UserAccount user = ((AuthenticatedUser) auth.getPrincipal()).getUser();
        return ResponseEntity.ok(subscriptionService.getEliteAccessStatus(user.getId()));
    }

    @GetMapping("/list")
    public PortfolioPageResponse list(
            @RequestParam JobCategory category,
            Pageable pageable) {
        return service.list(category, pageable);
    }

    @GetMapping("/user/premium")
    public PortfolioPageResponse getPremiumUsers(Authentication authuser, Pageable pageable) {
        return service.getPremiumPortfolios(authuser, pageable);
    }

    @PostMapping("/{id}/like")
    public void like(@PathVariable Long id, Authentication auth) {
        UserAccount user = ((AuthenticatedUser) auth.getPrincipal()).getUser();
        service.like(id, user);
    }
}
