package com.gametout.gametout.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.Builder;
import com.gametout.gametout.enums.StudiosEnum;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudiosDTO{
    private Long id;

    @NotBlank
    @Size(max = 255)
    private String studioName;

    private String studioLogoUrl;

    private String studioDescription;

    private String studioWebsiteUrl;

    @Min(1)
    @Max(5)
    private Short ratings;
    private String country;
    private String city;
    private String description;
    private Integer employeesCount; 
    private Double latitude;
    private Double longitude;
    private StudiosEnum status;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    
}
