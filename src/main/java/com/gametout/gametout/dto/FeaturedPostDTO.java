package com.gametout.gametout.dto;

import com.gametout.gametout.enums.PostEnum;
import java.io.Serializable;
import java.time.LocalDateTime;

public record FeaturedPostDTO(
    Long id,
    PostEnum postType,
    LocalDateTime featuredAt,
    BlogPostResponseDTO postDetails,
    PortfolioResponseDTO portfolioDetails,
    StudiosDTO studioDetails

) implements Serializable {}
