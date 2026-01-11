package com.gametout.gametout.dto;
import com.gametout.gametout.enums.JobCategory;
import com.gametout.gametout.enums.JobProfileStatus;

import java.util.List;


public record PortfolioRequest(
    String name,
    String shortDescription,
    String location,
    Integer experienceYears,
    JobCategory jobCategory,
    JobProfileStatus jobStatus,
    String profileSummary,
    String coverPhotoUrl,
    String profilePhotoUrl,
    String contactEmail,
    List<SkillDTO> skills,
    List<SocialLinkDTO> socials,
    String resumeUrl

) {}



