package com.gametout.gametout.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.gametout.gametout.repository.PostTagsRepository;

@Service
public class PostTagsService {

    private final PostTagsRepository repository;

    public PostTagsService(PostTagsRepository repository) {
        this.repository = repository;
    }

    @Cacheable(
        value = "tags",
        key = "'tag:' + #tag"
    )
    public List<Object> getPostsByTag(String tag) {
        return repository.findPostsByTag(tag);
    }
}
