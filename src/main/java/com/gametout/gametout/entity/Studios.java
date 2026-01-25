package com.gametout.gametout.entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.gametout.gametout.enums.StudiosEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;


@Entity
@Table(name = "studios",indexes = {
    @Index(name = "idx_studio_country", columnList = "country"),
    @Index(name = "idx_studio_city", columnList = "city"),
    @Index(name = "idx_studio_ratings", columnList = "ratings"),
    @Index(name = "idx_studio_status", columnList = "status"),
    @Index(name = "idx_studio_avg_rating", columnList = "average_rating")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Studios {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "studio_name", nullable = false)
    private String studioName;

    @Column(name = "studio_logo_url", columnDefinition = "text")
    private String studioLogoUrl;

    @Column(name = "studio_description", columnDefinition = "text")
    private String studioDescription;

    @Column(name = "studio_website_url", columnDefinition = "text")
    private String studioWebsiteUrl;

    @Column(name = "ratings", nullable = false, columnDefinition = "SMALLINT")
    @Min(1)
    @Max(5)
    private Short ratings;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "city", nullable = false)
    private String city;
    
    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "employees_count", nullable = false)
    private Integer employeesCount; 

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    // New rating stats fields (computed from studio_ratings table)
    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount = 0;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)  // New status field
    private StudiosEnum status;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = StudiosEnum.PENDING;
        }
        if (this.ratingCount == null) {
            this.ratingCount = 0;
        }
        if (this.averageRating == null) {
            this.averageRating = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
