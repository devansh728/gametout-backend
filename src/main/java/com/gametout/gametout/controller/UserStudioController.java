package com.gametout.gametout.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gametout.gametout.dto.StudiosDTO;
import com.gametout.gametout.entity.Studios;
import com.gametout.gametout.dto.StudioPageResponse;
import com.gametout.gametout.service.StudiosService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user/studio")
@RequiredArgsConstructor
public class UserStudioController {

    private final StudiosService studiosService;

    @PostMapping("/create-request")
    public ResponseEntity<StudiosDTO> createStudio(@RequestBody Studios studio) {
        return ResponseEntity.ok(studiosService.createStudioUser(studio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudiosDTO> getStudioById(@PathVariable Long id) {
        return ResponseEntity.ok(studiosService.getStudioById(id));
    }

    @GetMapping
    public ResponseEntity<StudioPageResponse> getAllStudios(Pageable pageable) {
        return ResponseEntity.ok(studiosService.getAllStudios(pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<StudioPageResponse> getStudiosByFilters(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Short ratings,
            Pageable pageable) {
        return ResponseEntity.ok(studiosService.getStudiosByFilters(country, city, ratings, pageable));
    }
}
