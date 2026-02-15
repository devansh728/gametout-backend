package com.gametout.gametout.service;

import com.gametout.gametout.dto.PortfolioPageResponse;
import com.gametout.gametout.dto.PortfolioRequest;
import com.gametout.gametout.dto.PortfolioResponseDTO;
import com.gametout.gametout.dto.SkillDTO;
import com.gametout.gametout.dto.UserSummaryDTO;
import com.gametout.gametout.entity.PortfolioProfile;
import com.gametout.gametout.entity.PortfolioResume;
import com.gametout.gametout.entity.PortfolioSkill;
import com.gametout.gametout.entity.PortfolioSocialLink;
import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.enums.JobCategory;
import com.gametout.gametout.enums.UserRole;
import com.gametout.gametout.repository.PortfolioRepository;
import com.gametout.gametout.repository.PortfolioSkillRepository;
import com.gametout.gametout.repository.PortfolioSocialRepository;
import com.gametout.gametout.entity.PortfolioLike;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import com.gametout.gametout.dto.SocialLinkDTO;
import com.gametout.gametout.repository.PortfolioResumeRepository;
import com.gametout.gametout.repository.PortfolioLikeRepository;
import org.springframework.security.core.Authentication;
import com.gametout.gametout.dto.AuthenticatedUser;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepo;
    private final PortfolioSkillRepository skillRepo;
    private final PortfolioSocialRepository socialRepo;
    private final PortfolioResumeRepository resumeRepo;
    private final PortfolioLikeRepository likeRepo;

    @CacheEvict(value = { "portfolio:list", "portfolio:user", "portfolio:count" }, allEntries = true)
    public PortfolioProfile createOrUpdate(
            UserAccount user,
            PortfolioRequest req) {
        PortfolioProfile portfolio = portfolioRepo.findByUserId(user.getId())
                .orElseGet(() -> {
                    PortfolioProfile p = new PortfolioProfile();
                    p.setUser(user);
                    // Premium is set based on user's subscription
                    p.setPremium(user.canCreatePremiumPortfolio());
                    return p;
                });

        // Update premium status on each save based on current subscription
        portfolio.setPremium(user.canCreatePremiumPortfolio());

        portfolio.setName(req.name());
        portfolio.setShortDescription(req.shortDescription());
        portfolio.setLocation(req.location());
        portfolio.setExperienceYears(req.experienceYears());
        portfolio.setJobCategory(req.jobCategory());
        portfolio.setJobStatus(req.jobStatus());
        portfolio.setProfileSummary(req.profileSummary());
        portfolio.setCoverPhotoUrl(req.coverPhotoUrl());
        portfolio.setProfilePhotoUrl(req.profilePhotoUrl());
        portfolio.setContactEmail(req.contactEmail());

        PortfolioProfile saved = portfolioRepo.save(portfolio);

        if (saved.getId() != null) {
            skillRepo.deleteByPortfolioId(saved.getId());
        }
        if (req.skills() != null && !req.skills().isEmpty()) {
            req.skills().forEach(s -> {
                PortfolioSkill skill = new PortfolioSkill();
                skill.setPortfolio(saved);
                skill.setSkillName(s.name());
                skill.setScore(s.score());
                skillRepo.save(skill);
            });
        }

        // Update socials - delete old ones only if portfolio already exists
        if (saved.getId() != null) {
            socialRepo.deleteByPortfolioId(saved.getId());
        }
        if (req.socials() != null && !req.socials().isEmpty()) {
            req.socials().forEach(s -> {
                PortfolioSocialLink link = new PortfolioSocialLink();
                link.setPortfolio(saved);
                link.setPlatform(s.platform());
                link.setUrl(s.url());
                socialRepo.save(link);
            });
        }

        // Update resume - only create/update if resumeUrl is provided
        if (req.resumeUrl() != null && !req.resumeUrl().trim().isEmpty()) {
            PortfolioResume resume = saved.getResume();
            if (resume == null) {
                // Create new resume if doesn't exist
                resume = new PortfolioResume();
                resume.setPortfolio(saved);
            }
            resume.setResumeUrl(req.resumeUrl());
            resume.setUploadedAt(java.time.LocalDateTime.now());
            resumeRepo.save(resume);
        }

        return saved;
    }

    @Cacheable(value = "portfolio:list", key = "#category + ':' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public PortfolioPageResponse list(
            JobCategory category,
            Pageable pageable) {
        Page<PortfolioProfile> page = portfolioRepo.listByCategory(category, pageable);
        Page<PortfolioResponseDTO> result = page.map(this::convertToDTO);
        return new PortfolioPageResponse(result);
    }

    @Transactional
    @CacheEvict(value = "portfolio:list", allEntries = true)
    public void like(Long portfolioId, UserAccount user) {

        boolean exists = likeRepo.existsByUserIdAndPortfolioId(user.getId(), portfolioId);

        if (exists) {
            likeRepo.deleteByUserIdAndPortfolioId(user.getId(), portfolioId);
            portfolioRepo.decrementLikes(portfolioId);
        } else {
            PortfolioLike like = new PortfolioLike();
            like.setUserId(user.getId());
            like.setPortfolioId(portfolioId);

            try {
                likeRepo.save(like);
                portfolioRepo.incrementLikes(portfolioId);
            } catch (DataIntegrityViolationException e) {
                throw new RuntimeException("You already liked this portfolio");
            }
        }

        // portfolioRepo.findById(portfolioId).ifPresent(p -> {
        // p.setLikesCount(p.getLikesCount() + 1);
        // });
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "portfolio:detail", key = "#id")
    public PortfolioResponseDTO findById(Long id) {
        PortfolioProfile p = portfolioRepo.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        return convertToDTO(p);
    }

    public PortfolioPageResponse getPremiumPortfolios(Authentication authuser, Pageable pageable) {
        if (authuser == null || !authuser.isAuthenticated()) {
            throw new RuntimeException("Authentication required");
        }
        UserAccount user = ((AuthenticatedUser) authuser.getPrincipal()).getUser();
        if (user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("ADMIN role required");
        }
        Page<PortfolioProfile> premiumPortfolios = portfolioRepo.findByIsPremiumTrue(pageable);
        Page<PortfolioResponseDTO> dtos = premiumPortfolios.map(this::convertToDTO);
        return new PortfolioPageResponse(dtos);
    }

    /**
     * Get current user's own portfolio for editing
     * Uses findByUserIdWithDetails to eagerly load all related entities (skills, socials, resume)
     * to prevent LazyInitializationException during DTO conversion
     */
    @Transactional(readOnly = true)
    public Optional<PortfolioResponseDTO> getMyPortfolio(Long userId) {
        return portfolioRepo.findByUserIdWithDetails(userId)
                .map(this::convertToDTO);
    }

    /**
     * Check if user owns a portfolio
     */
    @Transactional(readOnly = true)
    public boolean isOwner(Long portfolioId, Long userId) {
        return portfolioRepo.findById(portfolioId)
                .map(p -> p.getUser().getId().equals(userId))
                .orElse(false);
    }

    /**
     * Upgrade portfolio to premium (called after payment verification)
     */
    @CacheEvict(value = { "portfolio:list", "portfolio:user", "portfolio:detail" }, allEntries = true)
    public void upgradeToPremium(Long userId) {
        portfolioRepo.findByUserId(userId).ifPresent(portfolio -> {
            portfolio.setPremium(true);
            portfolioRepo.save(portfolio);
        });
    }

    /**
     * Downgrade portfolio from premium (called when subscription expires)
     */
    @CacheEvict(value = { "portfolio:list", "portfolio:user", "portfolio:detail" }, allEntries = true)
    public void downgradeFromPremium(Long userId) {
        portfolioRepo.findByUserId(userId).ifPresent(portfolio -> {
            portfolio.setPremium(false);
            portfolioRepo.save(portfolio);
        });
    }

    /**
     * Get total count of portfolios (cached)
     */
    @Cacheable(value = "portfolio:count", key = "'total'")
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return portfolioRepo.count();
    }

    /**
     * Convert PortfolioProfile entity to DTO.
     * Uses UserSummaryDTO instead of raw UserAccount to prevent
     * sensitive data leakage and Redis serialization failures.
     */
    private PortfolioResponseDTO convertToDTO(PortfolioProfile p) {
        UserAccount user = p.getUser();
        UserSummaryDTO userSummary = (user != null)
                ? new UserSummaryDTO(user.getId(), p.getName())
                : null;

        PortfolioResponseDTO dto = new PortfolioResponseDTO();
        dto.setId(p.getId());
        dto.setUser(userSummary);
        dto.setName(p.getName());
        dto.setShortDescription(p.getShortDescription());
        dto.setLocation(p.getLocation());
        dto.setExperienceYears(p.getExperienceYears());
        dto.setJobCategory(p.getJobCategory());
        dto.setJobStatus(p.getJobStatus());
        dto.setPremium(p.isPremium());
        dto.setProfileSummary(p.getProfileSummary());
        dto.setLikesCount(p.getLikesCount());
        dto.setCoverPhotoUrl(p.getCoverPhotoUrl());
        dto.setProfilePhotoUrl(p.getProfilePhotoUrl());
        dto.setContactEmail(p.getContactEmail());
        dto.setResumeUrl(
                p.getResume() != null ? p.getResume().getResumeUrl() : null);
        dto.setSkills(p.getSkills().stream()
                .map(s -> new SkillDTO(s.getSkillName(), s.getScore()))
                .toList());
        dto.setSocials(
                p.getSocialLinks().stream().map(s -> new SocialLinkDTO(s.getPlatform(), s.getUrl())).toList());
        return dto;
    }
}