package com.gametout.gametout.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gametout.gametout.entity.PostContentBlocks;

@Repository
public interface PostContentBlocksRepository
        extends JpaRepository<PostContentBlocks, Long> {

    /* ============================
       FULL CONTENT RENDER
       ============================ */

    List<PostContentBlocks> findByBlogPostIdOrderByBlockOrderAsc(Long postId);

    /* ============================
       ADMIN
       ============================ */

    void deleteByBlogPostId(Long postId);
}
