package com.gametout.gametout.service;

import com.gametout.gametout.dto.PostCreateRequest;
import com.gametout.gametout.entity.BlogPosts;
import com.gametout.gametout.repository.PostRepository;
import com.gametout.gametout.repository.PostContentBlocksRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;



@Service
@Transactional
public class AdminPostService {

    private final PostRepository postRepo;
    private final PostContentBlocksRepository blockRepo;

    public AdminPostService(
            PostRepository postRepo,
            PostContentBlocksRepository blockRepo
    ) {
        this.postRepo = postRepo;
        this.blockRepo = blockRepo;
    }

    /* =========================
       CREATE POST
       ========================= */

    @CacheEvict(
        value = { "feed", "post", "typeFeed", "featured", "headlines" },
        allEntries = true
    )
    public BlogPosts createPost(PostCreateRequest req) {

        BlogPosts post = new BlogPosts();
        post.setTitle(req.title());
        post.setDescription(req.description());
        post.setThumbnailUrl(req.thumbnailUrl());
        post.setVideoEmbedUrl(req.videoEmbedUrl());
        post.setPostType(req.postType());
        post.setCategory(req.category());
        post.setPublishedAt(req.publishedAt());
        post.setTimelineDuration(req.timeline());
        post.setSocialLinks(new HashMap<>(req.socialLinks()));
        post.setPostStatus(req.postStatus());

        return postRepo.save(post);
    }

    /* =========================
       UPDATE POST
       ========================= */

    @CacheEvict(
        value = { "feed", "post", "typeFeed", "featured", "headlines" },
        allEntries = true
    )
    public BlogPosts updatePost(Long postId, PostCreateRequest req) {

        BlogPosts post = postRepo.findById(postId)
                .orElseThrow();

        post.setTitle(req.title());
        post.setDescription(req.description());
        post.setThumbnailUrl(req.thumbnailUrl());
        post.setVideoEmbedUrl(req.videoEmbedUrl());
        post.setCategory(req.category());
        post.setPublishedAt(req.publishedAt());
        post.setTimelineDuration(req.timeline());
        post.setSocialLinks(new HashMap<>(req.socialLinks()));
        post.setPostStatus(req.postStatus());

        return postRepo.save(post);
    }

    /* =========================
       DELETE POST (CASCADE SAFE)
       ========================= */

    @CacheEvict(
        value = { "feed", "post", "typeFeed", "featured", "headlines" },
        allEntries = true
    )
    public void deletePost(Long postId) {
        blockRepo.deleteByBlogPostId(postId);
        postRepo.deleteById(postId);
    }
}

