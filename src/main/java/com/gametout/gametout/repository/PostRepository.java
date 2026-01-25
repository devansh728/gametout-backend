package com.gametout.gametout.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import com.gametout.gametout.entity.BlogPosts;
import com.gametout.gametout.enums.PostEnum;
import com.gametout.gametout.enums.PostStatus;

@Repository
public interface PostRepository extends JpaRepository<BlogPosts, Long> {

    /*
     * ============================
     * MAIN PUBLIC FEED (KEYSET)
     * ============================
     */

    @Query("""
                SELECT p FROM BlogPosts p
                WHERE p.postStatus = :status
                  AND p.publishedAt < :cursor
                ORDER BY p.publishedAt DESC
            """)
    List<BlogPosts> findFeed(
            PostStatus status,
            LocalDateTime cursor,
            Pageable pageable);

    // First page
    List<BlogPosts> findTop20ByPostStatusOrderByPublishedAtDesc(PostStatus status);

    /*
     * ============================
     * FILTERS
     * ============================
     */

    @Query("""
                SELECT p FROM BlogPosts p
                WHERE p.postType = :type
                  AND p.postStatus = :status
                ORDER BY p.publishedAt DESC
            """)
    List<BlogPosts> findByType(
            PostEnum type,
            PostStatus status,
            Pageable pageable);

    @Query("""
                SELECT p FROM BlogPosts p
                WHERE p.category = :category
                  AND p.postStatus = :status
                ORDER BY p.publishedAt DESC
            """)
    List<BlogPosts> findByCategory(
            String category,
            PostStatus status,
            Pageable pageable);

    /*
     * ============================
     * SINGLE POST
     * ============================
     */

    Optional<BlogPosts> findByIdAndPostStatus(Long id, PostStatus status);

    @Query("""
                SELECT bp
                FROM BlogPosts bp
                LEFT JOIN FETCH bp.contentBlocks
                WHERE bp.id = :id AND bp.postStatus = :status
            """)
    Optional<BlogPosts> findPublishedWithBlocks(
            @Param("id") Long id,
            @Param("status") PostStatus status);

    /*
     * ============================
     * SEARCH (SIMPLE, FAST)
     * ============================
     */

    @Query("""
                SELECT p FROM BlogPosts p
                WHERE lower(p.title) LIKE lower(concat('%', :keyword, '%'))
                   OR lower(p.description) LIKE lower(concat('%', :keyword, '%'))
                ORDER BY p.publishedAt DESC
            """)
    List<BlogPosts> search(String keyword, Pageable pageable);

    /*
     * ============================
     * METRICS (SAFE COUNTERS)
     * ============================
     */

    /*
     * ============================
     * COUNTERS
     * ============================
     */
    long countByPostTypeAndPostStatus(PostEnum type, PostStatus status);

    long countByPostStatus(PostStatus status);

    @Modifying(clearAutomatically = true)
    @Query(value = """
                UPDATE blog_posts
                SET likes = likes + 1
                WHERE id = :postId
            """, nativeQuery = true)
    void incrementLikes(Long postId);
}
