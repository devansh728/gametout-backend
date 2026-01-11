package com.gametout.gametout.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gametout.gametout.entity.FeaturedPosts;
import com.gametout.gametout.enums.PostEnum;
import org.springframework.data.repository.query.Param;

@Repository
public interface FeaturedPostsRepository
        extends JpaRepository<FeaturedPosts, Long> {

    /* ============================
       HOMEPAGE FEATURED
       ============================ */

    List<FeaturedPosts> findAll();

    @Query("SELECT f FROM FeaturedPosts f " +
           "LEFT JOIN FETCH f.blogPost " +
           "LEFT JOIN FETCH f.portfolio " +
           "LEFT JOIN FETCH f.studio " +
           "ORDER BY f.featuredAt DESC")
    List<FeaturedPosts> findAllBy();


   //  @Query("SELECT f FROM FeaturedPosts f JOIN FETCH f.blogPost WHERE f.postType = :type ORDER BY f.featuredAt DESC")
   //  Optional<FeaturedPosts> findByPostType(@Param("type") PostEnum type);

    @Query("SELECT f FROM FeaturedPosts f " +
           "LEFT JOIN FETCH f.blogPost " +    
           "LEFT JOIN FETCH f.portfolio " +
           "LEFT JOIN FETCH f.studio " +      
           "WHERE f.postType = :type " +
           "ORDER BY f.featuredAt DESC")
    List<FeaturedPosts> findAllByPostType(@Param("type") PostEnum type);

    @Query("SELECT f FROM FeaturedPosts f WHERE f.postType = :type AND f.blogPost.id = :id")
    Optional<FeaturedPosts> findByPostTypeAndBlogPost(@Param("type") PostEnum type, @Param("id") Long id);

    @Query("SELECT f FROM FeaturedPosts f WHERE f.postType = :type AND f.portfolio.id = :id")
    Optional<FeaturedPosts> findByPostTypeAndPortfolio_Id(@Param("type") PostEnum type, @Param("id") Long id);

    @Query("SELECT f FROM FeaturedPosts f WHERE f.postType = :type AND f.studio.id = :id")
    Optional<FeaturedPosts> findByPostTypeAndStudio_Id(@Param("type") PostEnum type, @Param("id") Long id);


   //  Optional<FeaturedPosts> findByPostType(PostEnum postType);

    /* ============================
       ADMIN
       ============================ */

    void deleteByPostType(PostEnum postType);
}
