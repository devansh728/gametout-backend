package com.gametout.gametout.entity;

import com.gametout.gametout.enums.BlockType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "post_content_blocks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostContentBlocks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @ToString.Exclude
    private BlogPosts blogPost;

    @Column(name = "block_order", nullable = false)
    private Integer blockOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type")
    private BlockType blockType;

    @Column(name = "text_content")
    private String textContent;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "caption")
    private String caption;
}
