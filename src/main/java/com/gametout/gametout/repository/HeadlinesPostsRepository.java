package com.gametout.gametout.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gametout.gametout.entity.HeadlinesPosts;

@Repository
public interface HeadlinesPostsRepository
        extends JpaRepository<HeadlinesPosts, Long> {

    /* ============================
       HOMEPAGE HEADLINES
       ============================ */

    List<HeadlinesPosts> findAllByOrderByPriorityAsc();

    /* ============================
       ADMIN
       ============================ */

    Optional<HeadlinesPosts> findByBlogPostId(Long postId);

    @Query("SELECT COUNT(h) FROM HeadlinesPosts h")
    long countHeadlines();

    void deleteByBlogPostId(Long postId);
}
