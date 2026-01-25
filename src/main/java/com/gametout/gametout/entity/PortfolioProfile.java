package com.gametout.gametout.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gametout.gametout.enums.JobCategory;
import com.gametout.gametout.enums.JobProfileStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;



@Entity
@Table(
    name = "portfolio_profiles",
    indexes = {
        @Index(name = "idx_portfolio_category", columnList = "jobCategory"),
        @Index(name = "idx_portfolio_premium", columnList = "isPremium"),
        @Index(name = "idx_portfolio_status", columnList = "jobStatus")
    }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private UserAccount user;

    private String name;

    @Column(length = 300)
    private String shortDescription;

    private String location;

    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    private JobCategory jobCategory;

    @Enumerated(EnumType.STRING)
    private JobProfileStatus jobStatus;

    private boolean isPremium;

    @Column(columnDefinition = "text")
    private String profileSummary;

    private Integer likesCount = 0;

    private String coverPhotoUrl;
    private String profilePhotoUrl;
    private String contactEmail;

    @OneToOne(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PortfolioResume resume;

    @OneToMany(mappedBy = "portfolio")
    @BatchSize(size=20)
    private List<PortfolioSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "portfolio")
    @BatchSize(size=20)
    private List<PortfolioSocialLink> socialLinks = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

