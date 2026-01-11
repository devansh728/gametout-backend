package com.gametout.gametout.dto;
import com.gametout.gametout.enums.JobProfileStatus;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PortfolioCardDTO {
    private Long id;
    private String name;
    private String profilePhotoUrl;
    private String shortDescription;
    private String location;
    private Integer experienceYears;
    private boolean isPremium;
    private JobProfileStatus jobStatus;
    
    private List<SkillCardDTO> skills;

    @Data @Builder
    public static class SkillCardDTO {
        private String name;
        private Integer score;
    }
}
