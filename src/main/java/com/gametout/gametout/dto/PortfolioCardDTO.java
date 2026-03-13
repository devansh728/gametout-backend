package com.gametout.gametout.dto;
import com.gametout.gametout.enums.JobProfileStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillCardDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private Integer score;
    }
}
