package com.gametout.gametout.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gametout.gametout.dto.AuthenticatedUser;
import com.gametout.gametout.dto.StudioRatingDTO;
import com.gametout.gametout.dto.StudioRatingRequest;
import com.gametout.gametout.dto.StudiosDTO;
import com.gametout.gametout.entity.Studios;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.dto.StudioPageResponse;
import com.gametout.gametout.service.StudiosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.gametout.gametout.enums.StudioCategory;
import com.gametout.gametout.enums.HiringStatus;
import java.util.Map;

@RestController
@RequestMapping("/api/user/studio")
@RequiredArgsConstructor
public class UserStudioController {

    private final StudiosService studiosService;

    @PostMapping("/create-request")
    public ResponseEntity<StudiosDTO> createStudio(@RequestBody Studios studio) {
        return ResponseEntity.ok(studiosService.createStudioUser(studio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudiosDTO> getStudioById(@PathVariable Long id) {
        return ResponseEntity.ok(studiosService.getStudioById(id));
    }

    @GetMapping
    public ResponseEntity<StudioPageResponse> getAllStudios(Pageable pageable) {
        return ResponseEntity.ok(studiosService.getAllStudios(pageable));
    }

    /**
     * Get count of approved studios
     * GET /api/user/studio/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getApprovedCount() {
        long count = studiosService.getApprovedCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/filter")
    public ResponseEntity<StudioPageResponse> getStudiosByFilters(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Short ratings,
            @RequestParam(required = false) StudioCategory category,
            @RequestParam(required = false) HiringStatus hiringStatus,
            Pageable pageable) {
        return ResponseEntity.ok(studiosService.getStudiosByFilters(country, city, ratings, category, hiringStatus, pageable));
    }

    /**
     * Submit or update a rating for a studio
     * POST /api/user/studio/{id}/rate
     */
    @PostMapping("/{id}/rate")
    public ResponseEntity<StudioRatingDTO> rateStudio(
            @PathVariable Long id,
            @Valid @RequestBody StudioRatingRequest request,
            Authentication auth) {
        UserAccount user = ((AuthenticatedUser) auth.getPrincipal()).getUser();
        StudioRatingDTO result = studiosService.rateStudio(id, request.getRating(), user);
        return ResponseEntity.ok(result);
    }

    /**
     * Get current user's rating for a studio
     * GET /api/user/studio/{id}/rating
     */
    @GetMapping("/{id}/rating")
    public ResponseEntity<StudioRatingDTO> getUserRating(
            @PathVariable Long id,
            Authentication auth) {
        UserAccount user = ((AuthenticatedUser) auth.getPrincipal()).getUser();
        StudioRatingDTO result = studiosService.getUserRating(id, user.getId());
        return ResponseEntity.ok(result);
    }

    /**
     * Get rating stats for a studio (public endpoint)
     * GET /api/user/studio/{id}/ratings
     */
    @GetMapping("/{id}/ratings")
    public ResponseEntity<StudioRatingDTO> getRatingStats(@PathVariable Long id) {
        StudioRatingDTO result = studiosService.getRatingStats(id);
        return ResponseEntity.ok(result);
    }
}
