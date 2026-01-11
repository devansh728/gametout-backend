package com.gametout.gametout.controller;

import org.springframework.web.bind.annotation.*;

import com.gametout.gametout.dto.PostCreateRequest;
import com.gametout.gametout.entity.BlogPosts;
import com.gametout.gametout.service.AdminPostService;


@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final AdminPostService service;

    public AdminPostController(AdminPostService service) {
        this.service = service;
    }

    @PostMapping
    public BlogPosts create(@RequestBody PostCreateRequest req) {
        return service.createPost(req);
    }

    @PutMapping("/{id}")
    public BlogPosts update(
            @PathVariable Long id,
            @RequestBody PostCreateRequest req
    ) {
        return service.updatePost(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deletePost(id);
    }
}

