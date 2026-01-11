package com.gametout.gametout.dto;

import com.gametout.gametout.entity.UserAccount;
import com.gametout.gametout.enums.JobCategory;
import com.gametout.gametout.enums.JobProfileStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import lombok.Builder;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioResponseDTO {
    private Long id;

    private UserAccount user;

    private String name;

    private String shortDescription;

    private String location;

    private Integer experienceYears;

    private JobCategory jobCategory;

    private JobProfileStatus jobStatus;

    private boolean isPremium;

    private String profileSummary;

    private Integer likesCount;

    private String coverPhotoUrl;

    private String profilePhotoUrl;

    private String contactEmail;

    private String resumeUrl;

    private List<SkillDTO> skills;

    private List<SocialLinkDTO> socials;
}


