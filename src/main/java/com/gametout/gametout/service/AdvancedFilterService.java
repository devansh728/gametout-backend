package com.gametout.gametout.service;

import com.gametout.gametout.dto.AdvancedFilterRequest;
import com.gametout.gametout.dto.PortfolioPageResponse;
import com.gametout.gametout.dto.PortfolioResponseDTO;
import com.gametout.gametout.entity.PortfolioProfile;
import com.gametout.gametout.enums.GameEngine;
import com.gametout.gametout.enums.JobCategory;
import com.gametout.gametout.enums.JobProfileStatus;
import com.gametout.gametout.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import com.gametout.gametout.dto.SkillDTO;
import com.gametout.gametout.dto.SocialLinkDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for advanced portfolio filtering
 * Handles conversion of filter request to database queries
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AdvancedFilterService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioService portfolioService;

    /**
     * Apply advanced filters from client request
     * 
     * LOGIC:
     * - null/empty list = no filter on that field (fetch all values)
     * - Multiple values in list = OR logic (show if matches ANY value)
     * - Multiple fields = AND logic (must match ALL fields specified)
     * - Skills: Normalized to lowercase for case-insensitive matching
     * - Location: Contains search, case-insensitive
     * 
     * @param request the filter criteria from client
     * @return paginated portfolio results matching filters
     */
    public PortfolioPageResponse applyAdvancedFilters(AdvancedFilterRequest request) {
        
        log.debug("Applying advanced filters: categories={}, statuses={}, skills={}, minExp={}, maxExp={}, engines={}, location={}",
            request.getJobCategories(),
            request.getJobStatuses(),
            request.getMinExperienceYears(),
            request.getMaxExperienceYears(),
            request.getEnginePreferences()
        );

        // Validate request
        validateFilterRequest(request);

        // Create pagination
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // Convert empty lists to null (signals "don't filter this field")
        List<JobCategory> jobCategories = isEmpty(request.getJobCategories()) ? null : request.getJobCategories();
        List<JobProfileStatus> jobStatuses = isEmpty(request.getJobStatuses()) ? null : request.getJobStatuses();
        List<GameEngine> enginePreferences = isEmpty(request.getEnginePreferences()) ? null : request.getEnginePreferences();
        
        // Normalize skill names to lowercase for case-insensitive matching
        // List<String> skillNames = normalizeSkillNames(request.getSkillNames());
        // skillNames = isEmpty(skillNames) ? null : skillNames;
        
        // Normalize location to trim whitespace and handle null
        // String location = (request.getLocation() == null || request.getLocation().trim().isEmpty()) 
        //     ? null 
        //     : request.getLocation().trim();

        // Execute query with all filters
        Page<PortfolioProfile> page = portfolioRepository.findByAdvancedFilters(
            jobCategories,
            jobStatuses,
            request.getMinExperienceYears(),
            request.getMaxExperienceYears(),
            enginePreferences,
            pageable
        );

        log.debug("Advanced filter query returned {} results out of {} total", 
            page.getNumberOfElements(), 
            page.getTotalElements()
        );

        // Convert to DTOs (same as existing flow)
        Page<PortfolioResponseDTO> dtos = page.map(this::convertToDTO);
        
        return new PortfolioPageResponse(dtos);
    }

    /**
     * Manual conversion to DTO since PortfolioService.convertToDTO is private.
     */
    private PortfolioResponseDTO convertToDTO(PortfolioProfile p) {
        return PortfolioResponseDTO.builder()
                .id(p.getId())
                .user(p.getUser() != null ? new com.gametout.gametout.dto.UserSummaryDTO(p.getUser().getId(), p.getName()) : null)
                .name(p.getName())
                .shortDescription(p.getShortDescription())
                .location(p.getLocation())
                .experienceYears(p.getExperienceYears())
                .jobCategory(p.getJobCategory())
                .jobStatus(p.getJobStatus())
                .isPremium(p.isPremium())
                .profileSummary(p.getProfileSummary())
                .likesCount(p.getLikesCount())
                .coverPhotoUrl(p.getCoverPhotoUrl())
                .profilePhotoUrl(p.getProfilePhotoUrl())
                .contactEmail(p.getContactEmail())
                .mobile(p.getMobile())
                .resumeUrl(p.getResume() != null ? p.getResume().getResumeUrl() : null)
                .skills(p.getSkills() != null ? p.getSkills().stream()
                        .map(s -> new SkillDTO(s.getSkillName(), s.getScore()))
                        .toList() : List.of())
                .socials(p.getSocialLinks() != null ? p.getSocialLinks().stream()
                        .map(s -> new SocialLinkDTO(s.getPlatform(), s.getUrl()))
                        .toList() : List.of())
                .enginePreference(p.getEnginePreference())
                .build();
    }

    /**
     * Validate filter request
     */
    private void validateFilterRequest(AdvancedFilterRequest request) {
        if (request.getPage() < 0) {
            throw new IllegalArgumentException("Page number must be >= 0");
        }
        if (request.getSize() <= 0 || request.getSize() > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        if (request.getMinExperienceYears() != null && request.getMinExperienceYears() < 0) {
            throw new IllegalArgumentException("Min experience years must be >= 0");
        }
        if (request.getMaxExperienceYears() != null && request.getMaxExperienceYears() < 0) {
            throw new IllegalArgumentException("Max experience years must be >= 0");
        }
        if (request.getMinExperienceYears() != null && request.getMaxExperienceYears() != null 
            && request.getMinExperienceYears() > request.getMaxExperienceYears()) {
            throw new IllegalArgumentException("Min experience years cannot be greater than max");
        }
    }

    /**
     * Check if list is null or empty
     */
    private <T> boolean isEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Normalize skill names for case-insensitive matching
     * - Converts to lowercase
     * - Trims whitespace
     * - Removes duplicates
     * - Filters out empty strings
     */
    private List<String> normalizeSkillNames(List<String> skillNames) {
        if (isEmpty(skillNames)) {
            return null;
        }
        
        return skillNames.stream()
            .filter(skill -> skill != null && !skill.trim().isEmpty())
            .map(skill -> skill.trim().toLowerCase())
            .distinct()
            .collect(Collectors.toList());
    }
}
