package com.gametout.gametout.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import com.gametout.gametout.dto.BlogPostResponseDTO;
import com.gametout.gametout.dto.BlogPostFeedDTO;
import com.gametout.gametout.entity.BlogPosts;
import com.gametout.gametout.enums.PostEnum;
import com.gametout.gametout.enums.PostStatus;
import com.gametout.gametout.service.BlogPostsService;
import com.gametout.gametout.service.BlogPostsService.BlogPostFeedList;

@RestController
@RequestMapping("/api/posts")
@Slf4j
public class BlogPostsController {

    private final BlogPostsService service;

    public BlogPostsController(BlogPostsService service) {
        this.service = service;
    }

    /* =========================
       MAIN FEED
       ========================= */

    @GetMapping
    // @CircuitBreaker(name = "contentService", fallbackMethod = "feedFallback")
    // @RateLimiter(name = "contentLimiter")
    public List<BlogPostFeedDTO> getFeed(
            @RequestParam(defaultValue = "PUBLISHED") PostStatus status,
            @RequestParam(required = false) LocalDateTime cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<BlogPostFeedDTO> posts = service.getFeed(
                status,
                cursor != null ? cursor : LocalDateTime.now(),
                size
        );
        log.info("Fetched {} posts", posts.size());
        log.info("Posts: {}", posts);
        return posts;
    }

    public List<BlogPosts> feedFallback(
            PostStatus status,
            LocalDateTime cursor,
            int size,
            Throwable ex
    ) {
        return List.of(); // degrade gracefully
    }

    /* =========================
       FULL SINGLE POST WITH BLOCKS
       ========================= */

    @GetMapping("/{id}")
    @CircuitBreaker(name = "contentService")
    public BlogPostResponseDTO getPost(@PathVariable Long id) {
        return service.getPublishedPost(id);
    }

    /* =========================
       TYPE FILTER
       ========================= */

    @GetMapping("/type/{type}")
    @CircuitBreaker(name = "contentService")
    public BlogPostFeedList getByType(
            @PathVariable PostEnum type,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getByType(type, size);
    }

    /* =========================
       LIKE
       ========================= */

    @PostMapping("/{id}/like")
    @RateLimiter(name = "contentLimiter")
    public void like(@PathVariable Long id) {
        service.likePost(id);
    }
}
