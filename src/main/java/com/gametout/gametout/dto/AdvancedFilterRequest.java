package com.gametout.gametout.dto;

import com.gametout.gametout.enums.GameEngine;
import com.gametout.gametout.enums.JobCategory;
import com.gametout.gametout.enums.JobProfileStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Advanced Filter Request DTO
 * Client sends this to filter portfolios by multiple criteria
 * 
 * Design: All filter fields are OPTIONAL (null/empty = no filter on that field)
 * Multiple values within a field use OR logic (e.g., status OPEN OR FREELANCE)
 * Multiple fields use AND logic (e.g., status=(OPEN|FREELANCE) AND engine=UNITY)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedFilterRequest {
    
    @JsonProperty("jobCategories")
    private List<JobCategory> jobCategories;  // null/empty = no filter
    
    @JsonProperty("jobStatuses")
    private List<JobProfileStatus> jobStatuses;  // null/empty = no filter
    
    @JsonProperty("skillNames")
    private List<String> skillNames;  // null/empty = no filter, case-insensitive search
    
    @JsonProperty("minExperienceYears")
    private Integer minExperienceYears;  // null = no lower limit
    
    @JsonProperty("maxExperienceYears")
    private Integer maxExperienceYears;  // null = no upper limit
    
    @JsonProperty("enginePreferences")
    private List<GameEngine> enginePreferences;  // null/empty = no filter
    
    @JsonProperty("location")
    private String location;  // null = no filter, contains search (case-insensitive)
    
    @JsonProperty("page")
    private int page = 0;
    
    @JsonProperty("size")
    private int size = 20;
}
