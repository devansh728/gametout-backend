package com.gametout.gametout.service;

import com.gametout.gametout.dto.StudioPageResponse;
import com.gametout.gametout.dto.StudioRatingDTO;
import com.gametout.gametout.dto.StudiosDTO;
import com.gametout.gametout.entity.Studios;
import com.gametout.gametout.entity.StudioRating;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.repository.StudiosRepository;
import com.gametout.gametout.repository.StudioRatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import com.gametout.gametout.enums.StudiosEnum;
import com.gametout.gametout.enums.StudioCategory;
import com.gametout.gametout.enums.HiringStatus;
import com.gametout.gametout.repository.StudioSpecification;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudiosService {

    private final StudiosRepository studiosRepository;
    private final StudioRatingRepository studioRatingRepository;

    /**
     * Get count of approved studios (cached)
     */
    @Cacheable(value = "studio:count", key = "'approved'")
    @Transactional(readOnly = true)
    public long getApprovedCount() {
        return studiosRepository.countByStatus(StudiosEnum.PUBLISHED);
    }

    @CacheEvict(value = { "studios", "studio:count" }, allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public StudiosDTO createStudio(Studios studio) {
        studio.setStatus(StudiosEnum.PUBLISHED);
        Studios savedStudio = studiosRepository.save(studio);
        return convertToDTO(savedStudio);
    }

    @CacheEvict(value = { "studios", "studio:count" }, allEntries = true)
    public StudiosDTO createStudioUser(Studios studio) {
        studio.setStatus(StudiosEnum.PENDING);
        Studios savedStudio = studiosRepository.save(studio);
        return convertToDTO(savedStudio);
    }

    @CacheEvict(value = "studios", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<StudiosDTO> createStudios(List<Studios> studios) {
        studios.forEach(studio -> studio.setStatus(StudiosEnum.PUBLISHED));
        List<Studios> savedStudios = studiosRepository.saveAll(studios);
        return savedStudios.stream().map(this::convertToDTO).toList();
    }

    @Cacheable(value = "studios", key = "#id")
    public StudiosDTO getStudioById(Long id) {
        Studios studio = studiosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Studio not found"));
        if (!studio.getStatus().equals(StudiosEnum.PUBLISHED)) {
            throw new RuntimeException("Studio is not public");
        }
        return convertToDTO(studio);
    }

    @Cacheable(value = "studios", key = "'all-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public StudioPageResponse getAllStudios(Pageable pageable) {
        return new StudioPageResponse(
                studiosRepository.findByStatus(StudiosEnum.PUBLISHED, pageable)
                        .map(this::convertToDTO));
    }

    @Cacheable(value = "studios", key = "'filter-' + #country + '-' + #city + '-' + #ratings + '-' + #category + '-' + #hiringStatus + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public StudioPageResponse getStudiosByFilters(String country, String city, Short ratings, 
            StudioCategory category, HiringStatus hiringStatus, Pageable pageable) {
        return new StudioPageResponse(
                studiosRepository.findAll(
                    StudioSpecification.withFilters(
                        StudiosEnum.PUBLISHED, country, city, ratings, category, hiringStatus
                    ),
                    pageable
                ).map(this::convertToDTO)
        );
    }

    @CachePut(value = "studios", key = "#studio.id")
    public StudiosDTO updateStudio(Studios studio) {
        studio.setStatus(StudiosEnum.PUBLISHED);
        Studios updatedStudio = studiosRepository.save(studio);
        return convertToDTO(updatedStudio);
    }

    @CacheEvict(value = "studios", key = "#id")
    public void deleteStudio(Long id) {
        studiosRepository.deleteById(id);
    }

    @CacheEvict(value = "studios", allEntries = true)
    public void deleteStudios(List<Long> ids) {
        studiosRepository.deleteAllById(ids);
    }

    // ==================== RATING METHODS ====================

    /**
     * Submit or update a rating for a studio
     */
    @CacheEvict(value = { "studios", "studio_rating" }, key = "#studioId")
    public StudioRatingDTO rateStudio(Long studioId, Short rating, UserAccount user) {
        Studios studio = studiosRepository.findById(studioId)
                .orElseThrow(() -> new RuntimeException("Studio not found"));

        if (!studio.getStatus().equals(StudiosEnum.PUBLISHED)) {
            throw new RuntimeException("Cannot rate unpublished studio");
        }

        // Find existing rating or create new one
        Optional<StudioRating> existingRating = studioRatingRepository
                .findByStudioIdAndUserId(studioId, user.getId());

        StudioRating studioRating;
        if (existingRating.isPresent()) {
            // Update existing rating
            studioRating = existingRating.get();
            studioRating.setRating(rating);
            log.info("User {} updated rating for studio {} to {}", user.getId(), studioId, rating);
        } else {
            // Create new rating
            studioRating = new StudioRating();
            studioRating.setStudio(studio);
            studioRating.setUser(user);
            studioRating.setRating(rating);
            log.info("User {} created new rating for studio {} with value {}", user.getId(), studioId, rating);
        }

        studioRatingRepository.save(studioRating);

        // The database trigger will update studio.average_rating and
        // studio.rating_count
        // But we need to refresh the entity to get updated values
        studiosRepository.flush();
        Studios updatedStudio = studiosRepository.findById(studioId).orElse(studio);

        return StudioRatingDTO.fromStatsWithUserRating(
                studioId,
                updatedStudio.getAverageRating(),
                updatedStudio.getRatingCount(),
                rating);
    }

    /**
     * Get user's rating for a specific studio
     */
    @Cacheable(value = "studio_rating", key = "#studioId + '-' + #userId")
    public StudioRatingDTO getUserRating(Long studioId, Long userId) {
        Studios studio = studiosRepository.findById(studioId)
                .orElseThrow(() -> new RuntimeException("Studio not found"));

        Short userRating = studioRatingRepository
                .findByStudioIdAndUserId(studioId, userId)
                .map(StudioRating::getRating)
                .orElse(null);

        return StudioRatingDTO.fromStatsWithUserRating(
                studioId,
                studio.getAverageRating(),
                studio.getRatingCount(),
                userRating);
    }

    /**
     * Get rating stats for a studio (without user-specific rating)
     */
    @Cacheable(value = "studio_rating", key = "#studioId + '-stats'")
    public StudioRatingDTO getRatingStats(Long studioId) {
        Studios studio = studiosRepository.findById(studioId)
                .orElseThrow(() -> new RuntimeException("Studio not found"));

        return StudioRatingDTO.fromStats(
                studioId,
                studio.getAverageRating(),
                studio.getRatingCount());
    }

    // ==================== END RATING METHODS ====================

    private StudiosDTO convertToDTO(Studios studio) {
        return StudiosDTO.builder()
                .id(studio.getId())
                .studioName(studio.getStudioName())
                .studioLogoUrl(studio.getStudioLogoUrl())
                .studioDescription(studio.getStudioDescription())
                .studioWebsiteUrl(studio.getStudioWebsiteUrl())
                .ratings(studio.getRatings())
                .averageRating(studio.getAverageRating())
                .ratingCount(studio.getRatingCount())
                .country(studio.getCountry())
                .city(studio.getCity())
                .description(studio.getDescription())
                .employeesCount(studio.getEmployeesCount())
                .latitude(studio.getLatitude())
                .longitude(studio.getLongitude())
                .createdAt(studio.getCreatedAt())
                .updatedAt(studio.getUpdatedAt())
                .status(studio.getStatus())
                .category(studio.getCategory())
                .hiringStatus(studio.getHiringStatus())
                .studioEmail(studio.getStudioEmail())
                .studioMobile(studio.getStudioMobile())
                .youtubeUrl(studio.getYoutubeUrl())
                .linkedinUrl(studio.getLinkedinUrl())
                .twitterUrl(studio.getTwitterUrl())
                .discordUrl(studio.getDiscordUrl())
                .build();
    }

    @Cacheable(value = "studios", key = "'pending-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public StudioPageResponse getPendingStudios(Pageable pageable) {
        return new StudioPageResponse(studiosRepository.findByStatus(StudiosEnum.PENDING, pageable)
                .map(this::convertToDTO));
    }

    @CacheEvict(value = "studios", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public boolean postApproved(Long id, boolean isApproved) {
        Studios studio = studiosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Studio not found"));

        if (isApproved) {
            studio.setStatus(StudiosEnum.PUBLISHED);
        } else {
            studio.setStatus(StudiosEnum.REJECTED);
        }
        studiosRepository.save(studio);
        return true;
    }
}