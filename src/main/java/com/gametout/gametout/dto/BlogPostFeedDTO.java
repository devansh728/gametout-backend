package com.gametout.gametout.dto;
import com.gametout.gametout.enums.PostEnum;
import com.gametout.gametout.enums.PostStatus;
import java.time.LocalDateTime;


public record BlogPostFeedDTO(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        String videoEmbedUrl,
        LocalDateTime publishedAt,
        PostEnum postType,
        PostStatus postStatus,
        Integer likes,
        Integer rates
) {}

