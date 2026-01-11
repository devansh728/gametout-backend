package com.gametout.gametout.service;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gametout.gametout.dto.ContentBlockRequest;
import com.gametout.gametout.entity.BlogPosts;
import com.gametout.gametout.entity.PostContentBlocks;
import com.gametout.gametout.repository.PostRepository;
import com.gametout.gametout.repository.PostContentBlocksRepository;


@Service
@Transactional
public class AdminContentBlockService {

    private final PostContentBlocksRepository blockRepo;
    private final PostRepository postRepo;

    public AdminContentBlockService(
            PostContentBlocksRepository blockRepo,
            PostRepository postRepo
    ) {
        this.blockRepo = blockRepo;
        this.postRepo = postRepo;
    }

    /* =========================
       REPLACE ALL BLOCKS (BEST)
       ========================= */

    @CacheEvict(value = "contentBlocks", key = "'blocks:' + #postId")
    public void replaceBlocks(
            Long postId,
            List<ContentBlockRequest> blocks
    ) {
        BlogPosts post = postRepo.findById(postId).orElseThrow();

        blockRepo.deleteByBlogPostId(postId);

        for (ContentBlockRequest req : blocks) {
            PostContentBlocks block = new PostContentBlocks();
            block.setBlogPost(post);
            block.setBlockOrder(req.order());
            block.setBlockType(req.blockType());
            block.setTextContent(req.textContent());
            block.setMediaUrl(req.mediaUrl());
            block.setCaption(req.caption());

            blockRepo.save(block);
        }
    }
}

