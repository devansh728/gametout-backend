package com.gametout.gametout.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import com.gametout.gametout.dto.FeaturedPostDTO;
import com.gametout.gametout.enums.PostEnum;
import com.gametout.gametout.service.FeaturedPostsService;

@RestController
@RequestMapping("/api/featured")
public class FeaturedPostsController {

    private final FeaturedPostsService service;

    public FeaturedPostsController(FeaturedPostsService service) {
        this.service = service;
    }

    @GetMapping
    @CircuitBreaker(name = "contentService")
    public List<FeaturedPostDTO> getFeatured() {
        return service.getAllFeaturedPosts();
    }

    @GetMapping("/{type}")
    public List<FeaturedPostDTO> getFeatured(
            @PathVariable PostEnum type
    ) {
        return service.getFeaturedByPostType(type);
    }

    @PostMapping("/{type}/{id}")
    public void setFeaturedById(
            @PathVariable PostEnum type,
            @PathVariable Long id
    ) {
        service.setFeaturedByID(type, id);
    }

    @DeleteMapping("/{type}/{id}")
    public void removeFeaturedById(
            @PathVariable PostEnum type,
            @PathVariable Long id
    ) {
        service.removeFeaturedById(type, id);
    }
}