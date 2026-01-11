package com.gametout.gametout.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.gametout.gametout.enums.PostEnum;
import com.gametout.gametout.enums.PostStatus;
import lombok.ToString;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "blog_posts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlogPosts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostEnum postType;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "thumbnail_url", columnDefinition = "text")
    private String thumbnailUrl;

    @Column(name = "video_embed_url", columnDefinition = "text")
    private String videoEmbedUrl;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

   
    private String timeline;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private HashMap<String, String> socialLinks = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "post_status", nullable = false)
    private PostStatus postStatus;

    @Column(name = "category")
    private String category;

    @OneToMany(mappedBy = "blogPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("blockOrder ASC")
    @ToString.Exclude
    private List<PostContentBlocks> contentBlocks = new ArrayList<>();

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(nullable = false)
    private Integer rates = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Transient
    public Duration getTimelineDuration() {
        return timeline != null ? Duration.parse(timeline) : null;
    }

    public void setTimelineDuration(Duration duration) {
        this.timeline = duration != null ? duration.toString() : null;
    }

}
