package com.gametout.gametout.service;

import java.util.stream.Collectors;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gametout.gametout.mapper.BlogPostMapper;
import com.gametout.gametout.dto.FeaturedPostDTO;
import com.gametout.gametout.entity.FeaturedPosts;
import com.gametout.gametout.enums.PostEnum;
import com.gametout.gametout.repository.FeaturedPostsRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import com.gametout.gametout.entity.BlogPosts;
import com.gametout.gametout.entity.PortfolioProfile;
import com.gametout.gametout.entity.Studios;


@Service
public class FeaturedPostsService {

    private final FeaturedPostsRepository repository;
    private final BlogPostMapper mapper;

    public FeaturedPostsService(FeaturedPostsRepository repository, BlogPostMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /* ---------------------------------------------------------
       1. GET ALL FEATURED POSTS (The Optimized Method)
       --------------------------------------------------------- */
    @Transactional(readOnly = true)
    @Cacheable(value = "featured", key = "'all'")
    public List<FeaturedPostDTO> getAllFeaturedPosts() {
        List<FeaturedPosts> allFeatured = repository.findAllBy();
        return allFeatured.stream()
                .map(mapper::toFeaturedDTO)
                .collect(Collectors.toList());
    }

    /* ---------------------------------------------------------
       2. LIST FEATURED POSTS
       --------------------------------------------------------- */
    @Transactional(readOnly = true)
    public List<FeaturedPostDTO> getFeaturedByPostType(PostEnum type) {
        List<FeaturedPosts> existing = repository.findAllByPostType(type);
        
        return existing.stream()
                .map(mapper::toFeaturedDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "featured", key = "'all'")
    public void removeFeaturedById(PostEnum type, Long id) {
        if(type.equals(PostEnum.DOCUMENTARIES) || type.equals(PostEnum.REVIEWS) || type.equals(PostEnum.PODCASTS)) {
            Optional<FeaturedPosts> existing = repository.findByPostTypeAndBlogPost(type, id);
            existing.ifPresent(featured -> repository.delete(featured));
        } else if(type.equals(PostEnum.PORTFOLIOS)) {
            Optional<FeaturedPosts> existing = repository.findByPostTypeAndPortfolio_Id(type, id);
            existing.ifPresent(featured -> repository.delete(featured));
        } else if(type.equals(PostEnum.STUDIOS)) {
            Optional<FeaturedPosts> existing = repository.findByPostTypeAndStudio_Id(type, id);
            existing.ifPresent(featured -> repository.delete(featured));
        }
    }

    @Transactional
    @CacheEvict(value = "featured", key = "'all'")
    public void setFeaturedByID(PostEnum type, Long postId) {
        if(type.equals(PostEnum.DOCUMENTARIES) || type.equals(PostEnum.REVIEWS) || type.equals(PostEnum.PODCASTS)) {
            FeaturedPosts newFeatured = new FeaturedPosts();
            if(repository.findByPostTypeAndBlogPost(type, postId).isPresent()) {
                return; // Already featured
            }
            newFeatured.setPostType(type);
            newFeatured.setBlogPost(new BlogPosts());
            newFeatured.getBlogPost().setId(postId);
            newFeatured.setFeaturedAt(LocalDateTime.now());
            repository.save(newFeatured);
        } else if(type.equals(PostEnum.PORTFOLIOS)) {
            FeaturedPosts newFeatured = new FeaturedPosts();
            if(repository.findByPostTypeAndPortfolio_Id(type, postId).isPresent()) {
                return; // Already featured
            }
            newFeatured.setPostType(type);
            newFeatured.setPortfolio(new PortfolioProfile());
            newFeatured.getPortfolio().setId(postId);
            newFeatured.setFeaturedAt(LocalDateTime.now());
            repository.save(newFeatured);
        } else if(type.equals(PostEnum.STUDIOS)) {
            FeaturedPosts newFeatured = new FeaturedPosts();
            if(repository.findByPostTypeAndStudio_Id(type, postId).isPresent()) {
                return; // Already featured
            }
            newFeatured.setPostType(type);
            newFeatured.setStudio(new Studios());
            newFeatured.getStudio().setId(postId);
            newFeatured.setFeaturedAt(LocalDateTime.now());
            repository.save(newFeatured);
        }
        else {
            throw new IllegalArgumentException("Unsupported PostEnum type for featured post.");
        }
    }
}
