package com.gametout.gametout.repository;
import com.gametout.gametout.entity.PortfolioProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioSearchRepository
        extends JpaRepository<PortfolioProfile, Long> {

    @Query(value = """
        SELECT p.*
        FROM portfolio_profiles p
        WHERE similarity(p.name, :query) > 0.2
        ORDER BY
          CASE WHEN p.is_premium = true THEN 0 ELSE 1 END,
          similarity(p.name, :query) DESC,
          p.likes_count DESC,
          p.updated_at DESC
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM portfolio_profiles p
        WHERE similarity(p.name, :query) > 0.2
        """,
        nativeQuery = true
    )
    Page<PortfolioProfile> searchByName(
        @Param("query") String query,
        Pageable pageable
    );
}

