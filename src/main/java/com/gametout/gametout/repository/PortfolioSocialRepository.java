package com.gametout.gametout.repository;
import com.gametout.gametout.entity.PortfolioSocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioSocialRepository
        extends JpaRepository<PortfolioSocialLink, Long> {

    void deleteByPortfolioId(Long portfolioId);
}
