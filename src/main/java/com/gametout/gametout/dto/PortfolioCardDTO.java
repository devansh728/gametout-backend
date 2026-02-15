package com.gametout.gametout.dto;
import com.gametout.gametout.enums.JobProfileStatus;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class PortfolioCardDTO implements Serializable {
    private static final long serialVersionUID = 1L;

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
    public static class SkillCardDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private Integer score;
    }
}
