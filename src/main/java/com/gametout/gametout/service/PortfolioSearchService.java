package com.gametout.gametout.service;
import com.gametout.gametout.dto.PortfolioCardDTO;
import com.gametout.gametout.entity.PortfolioProfile;
import com.gametout.gametout.repository.PortfolioSearchRepository;  
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gametout.gametout.dto.PortfolioCardPage;

@Service
@Transactional(readOnly = true)
public class PortfolioSearchService {

    private final PortfolioSearchRepository repo;

    public PortfolioSearchService(PortfolioSearchRepository repo) {
        this.repo = repo;
    }

    /**
     * Cached search (read-heavy)
     */
    @Cacheable(
        value = "portfolio:search",
        key = "'v2:' + (#query == null ? '' : #query.trim().toLowerCase()) + ':' + #pageable.pageNumber + ':' + #pageable.pageSize"
    )
    @Transactional(readOnly = true)
    public PortfolioCardPage search(
            String query,
            Pageable pageable
    ) {
        String normalizedQuery = query == null ? "" : query.trim();

        if (normalizedQuery.length() < 2) {
            return new PortfolioCardPage();
        }

        Page<PortfolioProfile> entities = repo.searchByName(normalizedQuery, pageable);

        return new PortfolioCardPage(entities.map(this::toCardDTO));
    }

    private PortfolioCardDTO toCardDTO(PortfolioProfile p) {
        return PortfolioCardDTO.builder()
            .id(p.getId())
            .name(p.getName())
            .profilePhotoUrl(p.getProfilePhotoUrl())
            .shortDescription(p.getShortDescription())
            .location(p.getLocation())
            .experienceYears(p.getExperienceYears())
            .isPremium(p.isPremium())
            .jobStatus(p.getJobStatus())
            .skills(p.getSkills().stream()
                .limit(5)
                .map(s -> PortfolioCardDTO.SkillCardDTO.builder()
                    .name(s.getSkillName())
                    .score(s.getScore())
                    .build())
                .toList())
            .build();
    }
}

