package com.gametout.gametout.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gametout.gametout.entity.PostContentBlocks;
import com.gametout.gametout.repository.PostContentBlocksRepository;

@Service
@Transactional(readOnly = true)
public class PostContentBlocksService {

    private final PostContentBlocksRepository repository;

    public PostContentBlocksService(PostContentBlocksRepository repository) {
        this.repository = repository;
    }

    @Cacheable(
        value = "contentBlocks",
        key = "'blocks:' + #postId"
    )
    public List<PostContentBlocks> getBlocks(Long postId) {
        return repository.findByBlogPostIdOrderByBlockOrderAsc(postId);
    }
}
