package com.gametout.gametout.repository;
import com.gametout.gametout.enums.JobCategory;
import com.gametout.gametout.enums.JobProfileStatus;
import com.gametout.gametout.entity.PortfolioProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<PortfolioProfile, Long> {

    Optional<PortfolioProfile> findByUserId(Long userId);

    /**
     * Find portfolio by user ID with all related entities eagerly loaded using EntityGraph
     * Prevents LazyInitializationException when accessing skills, socials, resume
     * 
     * EntityGraph approach solves Hibernate MultipleBagFetchException by:
     * - Avoiding cartesian product (multiple LEFT JOIN FETCH)
     * - Fetching each collection in separate queries
     * - Maintaining clean entity relationships
     */
    @Query("SELECT p FROM PortfolioProfile p WHERE p.user.id = :userId")
    @org.springframework.data.jpa.repository.EntityGraph(value = "portfolio.withAllDetails", type = org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD)
    Optional<PortfolioProfile> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("""
    SELECT p FROM PortfolioProfile p
    LEFT JOIN FETCH p.resume
    LEFT JOIN FETCH p.user
    WHERE p.jobCategory = :category
    ORDER BY
        CASE WHEN p.isPremium = true THEN 0 ELSE 1 END,  
        p.likesCount DESC, 
        p.createdAt DESC  
    """)
    Page<PortfolioProfile> listByCategory(
        @Param("category") JobCategory category,
        Pageable pageable
    );

    @Modifying
    @Query("UPDATE PortfolioProfile p SET p.likesCount = p.likesCount + 1 WHERE p.id = :id")
    void incrementLikes(@Param("id") Long id);

    @Modifying
    @Query("UPDATE PortfolioProfile p SET p.likesCount = p.likesCount - 1 WHERE p.id = :id")
    void decrementLikes(@Param("id") Long id);

    @Query("""
        SELECT p FROM PortfolioProfile p
        LEFT JOIN FETCH p.user
        LEFT JOIN FETCH p.resume
        WHERE p.id = :id
    """)
    Optional<PortfolioProfile> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT p FROM PortfolioProfile p
        LEFT JOIN FETCH p.user
        LEFT JOIN FETCH p.resume
        WHERE p.isPremium = true
        ORDER BY p.likesCount DESC, p.createdAt DESC
    """)
    Page<PortfolioProfile> findByIsPremiumTrue(Pageable pageable);

    /**
     * Filter portfolios by multiple categories and/or statuses.
     * Empty/null lists = no filter (fetch all).
     * Orders by: Premium first → Likes → Created date
     */
    @Query("""
        SELECT p FROM PortfolioProfile p
        LEFT JOIN FETCH p.resume
        LEFT JOIN FETCH p.user
        WHERE (:categories IS NULL OR p.jobCategory IN :categories)
          AND (:statuses IS NULL OR p.jobStatus IN :statuses)
        ORDER BY
            CASE WHEN p.isPremium = true THEN 0 ELSE 1 END,
            p.likesCount DESC,
            p.createdAt DESC
    """)
    Page<PortfolioProfile> findByFilters(
        @Param("categories") List<JobCategory> categories,
        @Param("statuses") List<JobProfileStatus> statuses,
        Pageable pageable
    );

    /**
     * Admin listing query with optional name search and category/status filters.
     */
    @Query("""
        SELECT p FROM PortfolioProfile p
        LEFT JOIN FETCH p.resume
        LEFT JOIN FETCH p.user
        WHERE (:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')))
          AND (:categories IS NULL OR p.jobCategory IN :categories)
          AND (:statuses IS NULL OR p.jobStatus IN :statuses)
        ORDER BY
            CASE WHEN p.isPremium = true THEN 0 ELSE 1 END,
            p.likesCount DESC,
            p.createdAt DESC
    """)
    Page<PortfolioProfile> findForAdmin(
        @Param("query") String query,
        @Param("categories") List<JobCategory> categories,
        @Param("statuses") List<JobProfileStatus> statuses,
        Pageable pageable
    );

    /**
     * Advanced multi-field filtering with optional criteria:
     * - jobCategories: multiple values with OR logic (matches ANY in list)
     * - jobStatuses: multiple values with OR logic (matches ANY in list)
     * - enginePreferences: multiple values with OR logic (matches ANY in list)
     * - experience: range filter (minExp <= experienceYears <= maxExp)
     * - location: contains search, case-insensitive
     * - skillNames: case-insensitive matching (normalizes both stored and input to lowercase)
     *
     * All filters combined with AND logic (must satisfy ALL specified fields)
     * Empty/null lists = don't filter on that field
     * 
     * Removed DISTINCT to avoid SELECT DISTINCT ORDER BY constraint violation in PostgreSQL
     * Uses LEFT JOIN FETCH for eager loading instead of subqueries to maintain readability
     */
    // @Query("""
    //     SELECT p FROM PortfolioProfile p
    //     LEFT JOIN FETCH p.user
    //     LEFT JOIN FETCH p.resume
    //     LEFT JOIN p.skills ps
    //     WHERE 
    //         (:jobCategories IS NULL OR p.jobCategory IN :jobCategories)
    //         AND (:jobStatuses IS NULL OR p.jobStatus IN :jobStatuses)
    //         AND (:minExperience IS NULL OR p.experienceYears >= :minExperience)
    //         AND (:maxExperience IS NULL OR p.experienceYears <= :maxExperience)
    //         AND (:enginePreferences IS NULL OR p.enginePreference IN :enginePreferences)
    //         AND (:location IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%')))
    //         AND (:skillNames IS NULL OR LOWER(ps.skillName) IN :skillNames)
    //     ORDER BY 
    //         CASE WHEN p.isPremium = true THEN 0 ELSE 1 END,
    //         p.likesCount DESC,
    //         p.createdAt DESC
    // """)
    // Page<PortfolioProfile> findByAdvancedFilters(
    //     @Param("jobCategories") List<JobCategory> jobCategories,
    //     @Param("jobStatuses") List<JobProfileStatus> jobStatuses,
    //     @Param("minExperience") Integer minExperienceYears,
    //     @Param("maxExperience") Integer maxExperienceYears,
    //     @Param("enginePreferences") List<com.gametout.gametout.enums.GameEngine> enginePreferences,
    //     @Param("location") String location,
    //     @Param("skillNames") List<String> skillNames,
    //     Pageable pageable
    // );

    @Query("""
        SELECT p FROM PortfolioProfile p
        LEFT JOIN FETCH p.user
        LEFT JOIN FETCH p.resume
        WHERE 
            (:jobCategories IS NULL OR p.jobCategory IN :jobCategories)
            AND (:jobStatuses IS NULL OR p.jobStatus IN :jobStatuses)
            AND (:minExperience IS NULL OR p.experienceYears >= :minExperience)
            AND (:maxExperience IS NULL OR p.experienceYears <= :maxExperience)
            AND (:enginePreferences IS NULL OR p.enginePreference IN :enginePreferences)
        ORDER BY 
            CASE WHEN p.isPremium = true THEN 0 ELSE 1 END,
            p.likesCount DESC,
            p.createdAt DESC
    """)
    Page<PortfolioProfile> findByAdvancedFilters(
        @Param("jobCategories") List<JobCategory> jobCategories,
        @Param("jobStatuses") List<JobProfileStatus> jobStatuses,
        @Param("minExperience") Integer minExperienceYears,
        @Param("maxExperience") Integer maxExperienceYears,
        @Param("enginePreferences") List<com.gametout.gametout.enums.GameEngine> enginePreferences,
        Pageable pageable
    );
}

