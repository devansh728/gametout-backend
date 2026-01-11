package com.gametout.gametout.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gametout.gametout.dto.StudioPageResponse;
import com.gametout.gametout.dto.StudiosDTO;
import java.util.List;
import org.springframework.data.domain.Pageable;
import com.gametout.gametout.entity.Studios;
import com.gametout.gametout.service.StudiosService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/admin/studios")
@RequiredArgsConstructor
public class AdminStudioController {
    
    private final StudiosService studiosService;

    @PostMapping
    public ResponseEntity<StudiosDTO> createStudio(@RequestBody Studios studio) {
        return ResponseEntity.ok(studiosService.createStudio(studio));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<StudiosDTO>> createStudios(@RequestBody List<Studios> studios) {
        return ResponseEntity.ok(studiosService.createStudios(studios));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudiosDTO> updateStudio(@PathVariable Long id, @RequestBody Studios studio) {
        studio.setId(id);
        return ResponseEntity.ok(studiosService.updateStudio(studio));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudio(@PathVariable Long id) {
        studiosService.deleteStudio(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Void> deleteStudios(@RequestBody List<Long> ids) {
        studiosService.deleteStudios(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending-studios")
    public StudioPageResponse getPendingStudios(Pageable pageable) {
        return studiosService.getPendingStudios(pageable);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Boolean> postApproved(@PathVariable Long id, @RequestParam boolean isApproved) {
        return ResponseEntity.ok(studiosService.postApproved(id, isApproved));
    }
    
}
