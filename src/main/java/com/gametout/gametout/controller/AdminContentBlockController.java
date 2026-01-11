package com.gametout.gametout.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.gametout.gametout.dto.ContentBlockRequest;
import com.gametout.gametout.service.AdminContentBlockService;

@RestController
@RequestMapping("/api/admin/posts/{postId}/blocks")
public class AdminContentBlockController {

    private final AdminContentBlockService service;

    public AdminContentBlockController(AdminContentBlockService service) {
        this.service = service;
    }

    @PutMapping
    public void replaceBlocks(
            @PathVariable Long postId,
            @RequestBody List<ContentBlockRequest> blocks
    ) {
        service.replaceBlocks(postId, blocks);
    }
}

