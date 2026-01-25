package com.gametout.gametout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for studio rating information
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudioRatingDTO {
    
    private Long studioId;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private Short userRating; // Current user's rating (null if not rated)
    
    /**
     * Create DTO with just stats (no user rating)
     */
    public static StudioRatingDTO fromStats(Long studioId, BigDecimal avgRating, Integer count) {
        return StudioRatingDTO.builder()
            .studioId(studioId)
            .averageRating(avgRating != null ? avgRating : BigDecimal.ZERO)
            .ratingCount(count != null ? count : 0)
            .userRating(null)
            .build();
    }
    
    /**
     * Create DTO with user's rating included
     */
    public static StudioRatingDTO fromStatsWithUserRating(
            Long studioId, 
            BigDecimal avgRating, 
            Integer count,
            Short userRating) {
        return StudioRatingDTO.builder()
            .studioId(studioId)
            .averageRating(avgRating != null ? avgRating : BigDecimal.ZERO)
            .ratingCount(count != null ? count : 0)
            .userRating(userRating)
            .build();
    }
}
