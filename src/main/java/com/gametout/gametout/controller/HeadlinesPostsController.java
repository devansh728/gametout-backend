package com.gametout.gametout.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import com.gametout.gametout.entity.HeadlinesPosts;
import com.gametout.gametout.service.HeadlinesPostsService;

@RestController
@RequestMapping("/api/headlines")
public class HeadlinesPostsController {

    private final HeadlinesPostsService service;

    public HeadlinesPostsController(HeadlinesPostsService service) {
        this.service = service;
    }

    @GetMapping
    @CircuitBreaker(name = "contentService")
    public List<HeadlinesPosts> getHeadlines() {
        return service.getHeadlines();
    }

    /* =========================
       ADMIN
       ========================= */

    @PostMapping
    public void addHeadline(@RequestBody HeadlinesPosts post) {
        service.addHeadline(post);
    }

    @DeleteMapping("/{postId}")
    public void removeHeadline(@PathVariable Long postId) {
        service.removeHeadline(postId);
    }
}
