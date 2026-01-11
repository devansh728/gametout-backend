package com.gametout.gametout.repository;
import com.gametout.gametout.entity.PortfolioResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioResumeRepository  extends JpaRepository<PortfolioResume, Long> {

    void deleteByPortfolioId(Long portfolioId);
    
}
