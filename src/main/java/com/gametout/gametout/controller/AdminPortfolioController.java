package com.gametout.gametout.controller;

import com.gametout.gametout.dto.PortfolioPageResponse;
import com.gametout.gametout.enums.JobCategory;
import com.gametout.gametout.enums.JobProfileStatus;
import com.gametout.gametout.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/portfolios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    public PortfolioPageResponse listForAdmin(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<JobCategory> categories,
            @RequestParam(required = false) List<JobProfileStatus> statuses,
            Pageable pageable
    ) {
        return portfolioService.listForAdmin(q, categories, statuses, pageable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        portfolioService.adminDeletePortfolio(id);
        return ResponseEntity.noContent().build();
    }
}
