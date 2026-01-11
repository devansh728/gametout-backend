package com.gametout.gametout.mapper;
import com.gametout.gametout.dto.BlogPostResponseDTO;
import com.gametout.gametout.dto.ContentBlockDTO;
import com.gametout.gametout.dto.FeaturedPostDTO;
import com.gametout.gametout.entity.BlogPosts;
import com.gametout.gametout.entity.FeaturedPosts;
import com.gametout.gametout.entity.PostContentBlocks;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import com.gametout.gametout.dto.PortfolioResponseDTO;
import com.gametout.gametout.dto.StudiosDTO;
import com.gametout.gametout.entity.PortfolioProfile;
import com.gametout.gametout.entity.Studios;
import com.gametout.gametout.dto.SkillDTO;
import com.gametout.gametout.dto.SocialLinkDTO;


@Component
public class BlogPostMapper {

    public BlogPostResponseDTO toDTO(BlogPosts post) {
        if (post == null) {
            return null;
        }
        return BlogPostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .thumbnailUrl(post.getThumbnailUrl())
                .videoEmbedUrl(post.getVideoEmbedUrl())
                .publishedAt(post.getPublishedAt())
                .postType(post.getPostType())
                .postStatus(post.getPostStatus())
                .likes(post.getLikes())
                .rates(post.getRates())
                .category(post.getCategory().toString())
                .contentBlocks(
                        post.getContentBlocks().stream()
                                .sorted(Comparator.comparingInt(PostContentBlocks::getBlockOrder))
                                .map(this::toBlockDTO)
                                .toList()
                )
                .build();

    }

    public FeaturedPostDTO toFeaturedDTO(FeaturedPosts featuredEntity) {
        return new FeaturedPostDTO(
            featuredEntity.getId(),
            featuredEntity.getPostType(),
            featuredEntity.getFeaturedAt(),
            toDTO(featuredEntity.getBlogPost()),
            toPortfolioDTO(featuredEntity.getPortfolio()),
            toStudioDTO(featuredEntity.getStudio())
        
        );
    }

    private ContentBlockDTO toBlockDTO(PostContentBlocks block) {
        return ContentBlockDTO.builder()
                .id(block.getId())
                .blockOrder(block.getBlockOrder())
                .blockType(block.getBlockType())
                .textContent(block.getTextContent())
                .mediaUrl(block.getMediaUrl())
                .caption(block.getCaption())
                .build();
                
    
    }

    private PortfolioResponseDTO toPortfolioDTO(PortfolioProfile portfolio) {
        if (portfolio == null) {
            return null;
        }
        return PortfolioResponseDTO.builder()
                .id(portfolio.getId())
                .user(null)
                .contactEmail(portfolio.getContactEmail())
                .coverPhotoUrl(portfolio.getCoverPhotoUrl())
                .profilePhotoUrl(portfolio.getProfilePhotoUrl())
                .name(portfolio.getName())
                .profileSummary(portfolio.getProfileSummary())
                .shortDescription(portfolio.getShortDescription())
                .location(portfolio.getLocation())
                .experienceYears(portfolio.getExperienceYears())
                .jobCategory(portfolio.getJobCategory())
                .jobStatus(portfolio.getJobStatus())
                .isPremium(portfolio.isPremium())
                .likesCount(portfolio.getLikesCount())
                .isPremium(portfolio.isPremium())
                .resumeUrl(portfolio.getResume().getResumeUrl())
                .skills(
                    portfolio.getSkills().stream()
                        .map(skill -> new SkillDTO(skill.getSkillName(), skill.getScore()))
                        .toList()
                )
                .socials(
                    portfolio.getSocialLinks().stream()
                        .map(link -> new SocialLinkDTO(link.getPlatform(), link.getUrl()))
                        .toList()
                )
                .build();
                

    }

    private StudiosDTO toStudioDTO(Studios studio) {
        if (studio == null) {
            return null;
        }
        return StudiosDTO.builder()
                .id(studio.getId())
                .studioName(studio.getStudioName())
                .studioLogoUrl(studio.getStudioLogoUrl())
                .studioDescription(studio.getStudioDescription())
                .studioWebsiteUrl(studio.getStudioWebsiteUrl())
                .ratings(studio.getRatings())
                .country(studio.getCountry())
                .city(studio.getCity())
                .description(studio.getDescription())
                .employeesCount(studio.getEmployeesCount())
                .latitude(studio.getLatitude())
                .longitude(studio.getLongitude())
                .createdAt(studio.getCreatedAt())
                .updatedAt(studio.getUpdatedAt())
                .createdAt(studio.getCreatedAt())
                .build();
    }

    
}

