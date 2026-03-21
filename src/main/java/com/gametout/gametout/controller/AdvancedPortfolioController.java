package com.gametout.gametout.controller;

import com.gametout.gametout.dto.AdvancedFilterRequest;
import com.gametout.gametout.dto.PortfolioPageResponse;
import com.gametout.gametout.service.AdvancedFilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Advanced Portfolio Filtering API
 * 
 * Provides complex filtering capabilities with multiple criteria
 */
@RestController
@RequestMapping("/api/portfolio/filter")
@RequiredArgsConstructor
@Slf4j
public class AdvancedPortfolioController {

    private final AdvancedFilterService advancedFilterService;

    /**
     * Apply advanced filters to portfolio search
     * 
     * POST /api/portfolio/filter
     * 
     * EXAMPLE REQUEST:
     * {
     *   "jobCategories": ["GAMEPLAY_PROGRAMMER", "ENGINE_PROGRAMMER"],
     *   "jobStatuses": ["OPEN", "FREELANCE"],
     *   "skillNames": ["C++", "Python"],
     *   "minExperienceYears": 3,
     *   "maxExperienceYears": 10,
     *   "enginePreferences": ["UNITY", "UNREAL"],
     *   "location": "San Francisco",
     *   "page": 0,
     *   "size": 20
     * }
     * 
     * FILTERING LOGIC:
     * - Each field is optional (null = don't filter by this field)
     * - Multiple values within a field: OR logic (if matches ANY value in list)
     * - Multiple fields: AND logic (must satisfy ALL specified fields)
     * - Skills: Case-insensitive matching (user input normalized to lowercase)
     * - Location: Case-insensitive contains search
     * - Experience: Range filter (minExp <= experienceYears <= maxExp)
     * 
     * ERROR RESPONSES:
     * - 400: Invalid filter request (bad page, size, experience range)
     * - 200: Returns PortfolioPageResponse with matching portfolios
     * 
     * @param request filter criteria from client
     * @return paginated list of matching portfolios
     */
    @PostMapping
    public ResponseEntity<PortfolioPageResponse> advancedFilter(
            @RequestBody AdvancedFilterRequest request) {
        
        try {
            log.info("Processing advanced filter request with {} job categories, {} statuses, {} skills",
                request.getJobCategories() != null ? request.getJobCategories().size() : 0,
                request.getJobStatuses() != null ? request.getJobStatuses().size() : 0,
                request.getSkillNames() != null ? request.getSkillNames().size() : 0
            );

            PortfolioPageResponse response = advancedFilterService.applyAdvancedFilters(request);
            
            log.info("Advanced filter completed. Found {} portfolios out of {} total",
                response.getContent().size(),
                response.getTotalElements()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid filter request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing advanced filter request", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
