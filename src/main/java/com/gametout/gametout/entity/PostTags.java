package com.gametout.gametout.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_tags")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostTags {

    @EmbeddedId
    private PostTagId id;

    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BlogPosts blogPost;

    @Column(name = "tag", insertable = false, updatable = false)
    private String tag;
}

