package com.gametout.gametout.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import com.gametout.gametout.entity.PostContentBlocks;
import com.gametout.gametout.service.PostContentBlocksService;

@RestController
@RequestMapping("/api/posts/{postId}/content")
public class PostContentBlocksController {

    private final PostContentBlocksService service;

    public PostContentBlocksController(PostContentBlocksService service) {
        this.service = service;
    }

    @GetMapping
    @CircuitBreaker(name = "contentService", fallbackMethod = "blocksFallback")
    public List<PostContentBlocks> getContent(@PathVariable Long postId) {
        return service.getBlocks(postId);
    }

    public List<PostContentBlocks> blocksFallback(Long postId, Throwable ex) {
        return List.of();
    }
}
