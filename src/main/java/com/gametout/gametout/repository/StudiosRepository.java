package com.gametout.gametout.repository;

import com.gametout.gametout.entity.Studios;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.gametout.gametout.enums.StudiosEnum;

@Repository
public interface StudiosRepository extends JpaRepository<Studios, Long>, JpaSpecificationExecutor<Studios> {

        Page<Studios> findByStatusAndCountryContainingIgnoreCaseAndCityContainingIgnoreCaseAndRatings(
                        StudiosEnum status, String country, String city, Short ratings, Pageable pageable);

        Page<Studios> findByStatusAndCountryContainingIgnoreCaseAndCityContainingIgnoreCase(
                        StudiosEnum status, String country, String city, Pageable pageable);

        Page<Studios> findByStatusAndRatings(StudiosEnum status, Short ratings, Pageable pageable);

        Page<Studios> findByStatus(StudiosEnum status, Pageable pageable);

        long countByStatus(StudiosEnum status);
}
//added hi
//gii