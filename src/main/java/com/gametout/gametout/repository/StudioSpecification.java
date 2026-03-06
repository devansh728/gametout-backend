package com.gametout.gametout.repository;

import com.gametout.gametout.entity.Studios;
import com.gametout.gametout.enums.StudiosEnum;
import com.gametout.gametout.enums.StudioCategory;
import com.gametout.gametout.enums.HiringStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class StudioSpecification {

    public static Specification<Studios> withFilters(
            StudiosEnum status,
            String country,
            String city,
            Short ratings,
            StudioCategory category,
            HiringStatus hiringStatus) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by published status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Country filter (contains, case insensitive)
            if (country != null && !country.isBlank()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("country")),
                    "%" + country.toLowerCase() + "%"
                ));
            }

            // City filter (contains, case insensitive)
            if (city != null && !city.isBlank()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("city")),
                    "%" + city.toLowerCase() + "%"
                ));
            }

            // Ratings filter (exact match)
            if (ratings != null) {
                predicates.add(criteriaBuilder.equal(root.get("ratings"), ratings));
            }

            // Category filter
            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            // Hiring status filter
            if (hiringStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("hiringStatus"), hiringStatus));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
