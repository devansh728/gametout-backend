package com.gametout.gametout.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gametout.gametout.entity.HeadlinesPosts;
import com.gametout.gametout.repository.HeadlinesPostsRepository;

@Service
public class HeadlinesPostsService {

    private static final int MAX_HEADLINES = 6;

    private final HeadlinesPostsRepository repository;

    public HeadlinesPostsService(HeadlinesPostsRepository repository) {
        this.repository = repository;
    }

    @Cacheable("headlines")
    public List<HeadlinesPosts> getHeadlines() {
        return repository.findAllByOrderByPriorityAsc();
    }

    @Transactional
    @CacheEvict(value = "headlines", allEntries = true)
    public void addHeadline(HeadlinesPosts post) {
        if (repository.countHeadlines() >= MAX_HEADLINES) {
            throw new IllegalStateException("Maximum 6 headlines allowed");
        }
        repository.save(post);
    }

    @Transactional
    @CacheEvict(value = "headlines", allEntries = true)
    public void removeHeadline(Long postId) {
        repository.deleteByBlogPostId(postId);
    }
}
