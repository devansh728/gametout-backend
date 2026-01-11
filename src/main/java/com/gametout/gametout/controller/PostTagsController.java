package com.gametout.gametout.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import com.gametout.gametout.service.PostTagsService;

@RestController
@RequestMapping("/api/tags")
public class PostTagsController {

    private final PostTagsService service;

    public PostTagsController(PostTagsService service) {
        this.service = service;
    }

    @GetMapping("/{tag}")
    @CircuitBreaker(name = "contentService")
    public List<Object> getByTag(@PathVariable String tag) {
        return service.getPostsByTag(tag);
    }
}
