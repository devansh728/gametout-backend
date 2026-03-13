package com.gametout.gametout.controller;

import com.gametout.gametout.service.PortfolioSearchService;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gametout.gametout.dto.PortfolioResponseDTO;
import com.gametout.gametout.service.PortfolioService;
import com.gametout.gametout.dto.PortfolioCardPage;

@RestController
@RequestMapping("/api/search/portfolio")
public class PortfolioSearchController {

    private final PortfolioSearchService service;
    private final PortfolioService portfolioService;

    public PortfolioSearchController(PortfolioSearchService service, PortfolioService portfolioService) {
        this.service = service;
        this.portfolioService = portfolioService;
    }

    @GetMapping
    // @RateLimiter(name = "searchLimiter")
    public PortfolioCardPage search(
            @RequestParam String q,
            Pageable pageable) {
        return service.search(q, pageable);
    }

    @GetMapping("/{id}")
    public PortfolioResponseDTO getDetails(@PathVariable Long id) {
        return portfolioService.findById(id);
    }
}
