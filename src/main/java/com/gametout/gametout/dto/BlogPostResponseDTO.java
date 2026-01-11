package com.gametout.gametout.dto;
import com.gametout.gametout.enums.PostStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

import com.gametout.gametout.enums.PostEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogPostResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String videoEmbedUrl;
    private LocalDateTime publishedAt;
    private PostEnum postType;
    private PostStatus postStatus;
    private int likes;
    private int rates;
    private String category;
    private List<ContentBlockDTO> contentBlocks;
}
