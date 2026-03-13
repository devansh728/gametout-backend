package com.gametout.gametout.repository;
import com.gametout.gametout.entity.PortfolioLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PortfolioLikeRepository extends JpaRepository<PortfolioLike, Long> {
    boolean existsByUserIdAndPortfolioId(Long userId, Long portfolioId);
    void deleteByUserIdAndPortfolioId(Long userId, Long portfolioId);
    void deleteByPortfolioId(Long portfolioId);
}
