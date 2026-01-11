package com.gametout.gametout.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gametout.gametout.entity.PostTags;
import com.gametout.gametout.entity.PostTagId;

@Repository
public interface PostTagsRepository
        extends JpaRepository<PostTags, PostTagId> {

    /* ============================
       TAG SEARCH
       ============================ */

    @Query("""
        SELECT pt.blogPost
        FROM PostTags pt
        WHERE pt.id.tag = :tag
        ORDER BY pt.blogPost.publishedAt DESC
    """)
    List<Object> findPostsByTag(String tag);

    /* ============================
       ADMIN
       ============================ */

    void deleteByBlogPostId(Long postId);
}
