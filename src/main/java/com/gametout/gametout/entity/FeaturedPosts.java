package com.gametout.gametout.entity;

import java.time.LocalDateTime;

import com.gametout.gametout.enums.PostEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "featured_posts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeaturedPosts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    private PostEnum postType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = true)
    private BlogPosts blogPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = true)
    private PortfolioProfile portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studio_id", nullable = true)
    private Studios studio;

    @Column(name = "featured_at", nullable = false)
    private LocalDateTime featuredAt;

    @PrePersist
    public void onCreate() {
        this.featuredAt = LocalDateTime.now();
    }

    @AssertTrue(message = "A featured item must link to exactly one type (Post, Portfolio, or Studio)")
    public boolean isValidReference() {
        int count = 0;
        if (blogPost != null)
            count++;
        if (portfolio != null)
            count++;
        if (studio != null)
            count++;
        return count == 1;
    }

}
