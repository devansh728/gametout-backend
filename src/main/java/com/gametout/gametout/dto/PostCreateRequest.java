package com.gametout.gametout.dto;
import com.gametout.gametout.enums.PostEnum;
import com.gametout.gametout.enums.PostStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;


public record PostCreateRequest(
        String title,
        String description,
        String thumbnailUrl,
        String videoEmbedUrl,
        PostEnum postType,
        String category,
        LocalDateTime publishedAt,
        Duration timeline,
        Map<String, String> socialLinks,
        PostStatus postStatus
) {}
