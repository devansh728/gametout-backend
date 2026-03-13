package com.gametout.gametout.dto;

import com.gametout.gametout.enums.JobCategory;
import com.gametout.gametout.enums.JobProfileStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PortfolioRequest(
    @NotBlank(message = "Name is required")
    String name,
    
    @NotBlank(message = "Role description is required")
    String shortDescription,
    
    @NotBlank(message = "Location is required")
    String location,
    
    Integer experienceYears,
    
    @NotNull(message = "Job category is required")
    JobCategory jobCategory,
    
    @NotNull(message = "Job status is required")
    JobProfileStatus jobStatus,
    
    String profileSummary,
    String coverPhotoUrl,
    String profilePhotoUrl,
    
    @NotBlank(message = "Contact email is required")
    String contactEmail,

    String mobile,
    
    List<SkillDTO> skills,
    List<SocialLinkDTO> socials,
    String resumeUrl
) {}



