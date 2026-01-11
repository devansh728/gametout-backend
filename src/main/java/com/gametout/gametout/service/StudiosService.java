package com.gametout.gametout.service;

import com.gametout.gametout.dto.StudioPageResponse;
import com.gametout.gametout.dto.StudiosDTO;
import com.gametout.gametout.entity.Studios;
import com.gametout.gametout.repository.StudiosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import com.gametout.gametout.enums.StudiosEnum;
import java.util.List;

@Service
@Transactional
public class StudiosService {

    @Autowired
    private StudiosRepository studiosRepository;

    @CacheEvict(value = "studios", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public StudiosDTO createStudio(Studios studio) {
        studio.setStatus(StudiosEnum.PUBLISHED);
        Studios savedStudio = studiosRepository.save(studio);
        return convertToDTO(savedStudio);
    }

    public StudiosDTO createStudioUser(Studios studio) {
        studio.setStatus(StudiosEnum.PENDING);
        Studios savedStudio = studiosRepository.save(studio);
        return convertToDTO(savedStudio);
    }

    @CacheEvict(value = "studios", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<StudiosDTO> createStudios(List<Studios> studios) {
        studios.forEach(studio -> studio.setStatus(StudiosEnum.PUBLISHED));
        List<Studios> savedStudios = studiosRepository.saveAll(studios);
        return savedStudios.stream().map(this::convertToDTO).toList();
    }

    @Cacheable(value = "studios", key = "#id")
    public StudiosDTO getStudioById(Long id) {
        Studios studio = studiosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Studio not found"));
        if (!studio.getStatus().equals(StudiosEnum.PUBLISHED)) {
             throw new RuntimeException("Studio is not public");
        }
        return convertToDTO(studio);
    }

    @Cacheable(value = "studios", key = "'all-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public StudioPageResponse getAllStudios(Pageable pageable) {
        return new StudioPageResponse(
            studiosRepository.findByStatus(StudiosEnum.PUBLISHED, pageable)
                             .map(this::convertToDTO)
        );
    }

    @Cacheable(value = "studios", key = "'filter-' + #country + '-' + #city + '-' + #ratings + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public StudioPageResponse getStudiosByFilters(String country, String city, Short ratings, Pageable pageable) {
        if (country != null && city != null && ratings != null) {
            return new StudioPageResponse(studiosRepository.findByStatusAndCountryContainingIgnoreCaseAndCityContainingIgnoreCaseAndRatings(
                    StudiosEnum.PUBLISHED, country, city, ratings, pageable).map(this::convertToDTO));
        } else if (country != null && city != null) {
            return new StudioPageResponse(studiosRepository.findByStatusAndCountryContainingIgnoreCaseAndCityContainingIgnoreCase(
                    StudiosEnum.PUBLISHED, country, city, pageable).map(this::convertToDTO));
        } else if (ratings != null) {
            return new StudioPageResponse(studiosRepository.findByStatusAndRatings(StudiosEnum.PUBLISHED, ratings, pageable).map(this::convertToDTO));
        } else {
            return new StudioPageResponse(studiosRepository.findByStatus(StudiosEnum.PUBLISHED, pageable).map(this::convertToDTO));
        }
    }

    @CachePut(value = "studios", key = "#studio.id")
    public StudiosDTO updateStudio(Studios studio) {
        studio.setStatus(StudiosEnum.PUBLISHED);
        Studios updatedStudio = studiosRepository.save(studio);
        return convertToDTO(updatedStudio);
    }

    @CacheEvict(value = "studios", key = "#id")
    public void deleteStudio(Long id) {
        studiosRepository.deleteById(id);
    }

    @CacheEvict(value = "studios", allEntries = true)
    public void deleteStudios(List<Long> ids) {
        studiosRepository.deleteAllById(ids);
    }

    private StudiosDTO convertToDTO(Studios studio) {
        return StudiosDTO.builder()
                .id(studio.getId())
                .studioName(studio.getStudioName())
                .studioLogoUrl(studio.getStudioLogoUrl())
                .studioDescription(studio.getStudioDescription())
                .studioWebsiteUrl(studio.getStudioWebsiteUrl())
                .ratings(studio.getRatings())
                .country(studio.getCountry())
                .city(studio.getCity())
                .description(studio.getDescription())
                .employeesCount(studio.getEmployeesCount())
                .latitude(studio.getLatitude())
                .longitude(studio.getLongitude())
                .createdAt(studio.getCreatedAt())
                .updatedAt(studio.getUpdatedAt())
                .status(studio.getStatus())
                .build();
    }

    @Cacheable(value = "studios", key = "'pending-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public StudioPageResponse getPendingStudios(Pageable pageable) {
        return new StudioPageResponse(studiosRepository.findByStatus(StudiosEnum.PENDING, pageable)
                .map(this::convertToDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public boolean postApproved(Long id, boolean isApproved) {
        Studios studio = studiosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Studio not found"));

        if (isApproved) {
            studio.setStatus(StudiosEnum.PUBLISHED);
        } else {
            studio.setStatus(StudiosEnum.REJECTED);
        }
        studiosRepository.save(studio);
        return true;
    }
}