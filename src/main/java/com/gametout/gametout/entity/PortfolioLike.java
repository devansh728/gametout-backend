package com.gametout.gametout.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "portfolio_likes",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "portfolio_id"}) // The Magic Constraint
    },
    indexes = {
        @Index(name = "idx_like_user_portfolio", columnList = "user_id, portfolio_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; 

    @Column(name = "portfolio_id", nullable = false)
    private Long portfolioId;

    private LocalDateTime likedAt;

    @PrePersist
    void onCreate() {
        this.likedAt = LocalDateTime.now();
    }
}
