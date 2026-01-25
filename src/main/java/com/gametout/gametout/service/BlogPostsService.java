package com.gametout.gametout.service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gametout.gametout.dto.BlogPostFeedDTO;
import com.gametout.gametout.dto.BlogPostResponseDTO;
import com.gametout.gametout.entity.BlogPosts;
import com.gametout.gametout.enums.PostEnum;
import com.gametout.gametout.enums.PostStatus;
import com.gametout.gametout.mapper.BlogPostMapper;
import com.gametout.gametout.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BlogPostsService {

    private final PostRepository repository;
    private final BlogPostMapper mapper;

    public BlogPostsService(PostRepository repository, RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.mapper = new BlogPostMapper();
    }

    /*
     * ================================
     * PUBLIC READ APIs (CACHED)
     * ================================
     */

    @Cacheable(value = "feed", key = "'feed:' + #status + ':' + #cursor")
    @Transactional(readOnly = true)
    public List<BlogPostFeedDTO> getFeed(
            PostStatus status,
            LocalDateTime cursor,
            int size) {
        log.info("Fetching feed with status: {}, cursor: {}, size: {}", status, cursor, size);
        List<BlogPosts> posts = repository.findFeed(
                status,
                cursor,
                PageRequest.of(0, size));
        log.info("Fetched {} posts", posts.size());
        return posts.stream()
                .map(this::toFeedDTO)
                .toList();
    }

    private BlogPostFeedDTO toFeedDTO(BlogPosts p) {
        return new BlogPostFeedDTO(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getThumbnailUrl(),
                p.getVideoEmbedUrl(),
                p.getPublishedAt(),
                p.getPostType(),
                p.getPostStatus(),
                p.getLikes(),
                p.getRates());
    }

    @Cacheable(value = "post", key = "'post:' + #postId")
    @Transactional(readOnly = true)
    public BlogPostResponseDTO getPublishedPost(Long postId) {
        BlogPosts posts = repository
                .findPublishedWithBlocks(postId, PostStatus.PUBLISHED)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return mapper.toDTO(posts);
    }

    public record BlogPostFeedList(
            List<BlogPostFeedDTO> posts) implements Serializable {
    }

    @Cacheable(value = "typeFeed", key = "'type:' + #type")
    @Transactional(readOnly = true)
    public BlogPostFeedList getByType(PostEnum type, int size) {
        List<BlogPosts> posts = repository.findByType(
                type,
                PostStatus.PUBLISHED,
                PageRequest.of(0, size));
        List<BlogPostFeedDTO> result = posts.stream()
                .map(this::toFeedDTO)
                .collect(Collectors.toList());
        return new BlogPostFeedList(result);
    }

    /*
     * ================================
     * COUNT APIs (CACHED)
     * ================================
     */

    @Cacheable(value = "post:count", key = "'type:' + #type")
    @Transactional(readOnly = true)
    public long getCountByType(PostEnum type) {
        return repository.countByPostTypeAndPostStatus(type, PostStatus.PUBLISHED);
    }

    @Cacheable(value = "post:count", key = "'total'")
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return repository.countByPostStatus(PostStatus.PUBLISHED);
    }

    /*
     * ================================
     * WRITE APIs (CACHE INVALIDATION)
     * ================================
     */

    @Transactional
    @CacheEvict(value = { "feed", "post", "typeFeed", "post:count" }, allEntries = true)
    public BlogPosts create(BlogPosts post) {
        return repository.save(post);
    }

    @Transactional
    @CacheEvict(value = { "feed", "post", "typeFeed", "post:count" }, key = "'post:' + #post.id")
    public BlogPosts update(BlogPosts post) {
        return repository.save(post);
    }

    /*
     * ================================
     * SAFE COUNTER UPDATE
     * ================================
     */

    @Transactional
    @CacheEvict(value = "post", key = "'post:' + #postId")
    public void likePost(Long postId) {
        repository.incrementLikes(postId);
    }
}
