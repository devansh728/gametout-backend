package com.gametout.gametout.dto;
import java.util.HashMap;

import com.gametout.gametout.enums.PostEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogPostDto {
    @NotBlank(message = "Title cannot be empty")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title; //
    @NotBlank(message = "Description cannot be empty")
    private String description;
    @NotBlank(message = "Content cannot be empty")
    private String content;
    private String thumbnailUrlString; //
    @NotNull(message = "Post status cannot be null")
    private PostEnum postEnum; //
    @NotBlank(message = "Category cannot be empty")
    private String Category; //
    private String tags; //
    private String dateCreated; //
    private HashMap<String, String> socialLinks; //
    @PositiveOrZero(message = "Likes count cannot be negative")
    private Integer likes; //
    @PositiveOrZero(message = "Rates count cannot be negative")
    private Integer rates; //
    private Duration timeline;
    private Boolean isFeatured;
    private Boolean isHeadline;
    private String RemovePostId;

}
