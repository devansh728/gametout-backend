package com.gametout.gametout.repository;

import com.gametout.gametout.entity.StudioRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudioRatingRepository extends JpaRepository<StudioRating, Long> {

    /**
     * Find user's rating for a specific studio
     */
    Optional<StudioRating> findByStudioIdAndUserId(Long studioId, Long userId);
    
    /**
     * Check if user has rated a studio
     */
    boolean existsByStudioIdAndUserId(Long studioId, Long userId);
    
    /**
     * Get all ratings for a studio
     */
    List<StudioRating> findByStudioId(Long studioId);
    
    /**
     * Get all ratings by a user
     */
    List<StudioRating> findByUserId(Long userId);
    
    /**
     * Count ratings for a studio
     */
    int countByStudioId(Long studioId);
    
    /**
     * Calculate average rating for a studio
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM StudioRating r WHERE r.studio.id = :studioId")
    BigDecimal calculateAverageRating(@Param("studioId") Long studioId);
    
    /**
     * Get rating stats for a studio
     */
    @Query("SELECT COUNT(r), COALESCE(AVG(r.rating), 0) FROM StudioRating r WHERE r.studio.id = :studioId")
    Object[] getRatingStats(@Param("studioId") Long studioId);
    
    /**
     * Delete all ratings for a studio
     */
    void deleteByStudioId(Long studioId);
    
    /**
     * Get user's ratings for multiple studios (batch query)
     */
    @Query("SELECT r FROM StudioRating r WHERE r.user.id = :userId AND r.studio.id IN :studioIds")
    List<StudioRating> findByUserIdAndStudioIdIn(@Param("userId") Long userId, @Param("studioIds") List<Long> studioIds);
}
