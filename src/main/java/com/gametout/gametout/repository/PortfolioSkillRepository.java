package com.gametout.gametout.repository;
import com.gametout.gametout.entity.PortfolioSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioSkillRepository
        extends JpaRepository<PortfolioSkill, Long> {

    void deleteByPortfolioId(Long portfolioId);
}
