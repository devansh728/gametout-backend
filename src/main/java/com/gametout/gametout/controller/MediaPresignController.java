package com.gametout.gametout.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.util.Map;
import com.gametout.gametout.dto.AuthenticatedUser;
import com.gametout.gametout.service.MediaPresignService;
import com.gametout.gametout.verification.EmailVerifiedRequired;

@RestController
@RequestMapping("/api/media/presign")
public class MediaPresignController {
    private final MediaPresignService service;

    public MediaPresignController(MediaPresignService service) {
        this.service = service;
    }

    @PostMapping("/presign")
    public Map<String, String> presign(
            @RequestParam String filename,
            @RequestParam String contentType) {
        // Public endpoint - no auth required
        return service.presignUpload(filename, contentType);
    }

    @PostMapping("/resume")
    public Map<String, String> presignResume(
            @RequestParam String filename) {
        return service.presignUpload(   
                filename, "application/pdf");
    }

    @PostMapping("/delete-direct")
    public Map<String, String> presignDelete(
            @RequestParam String objectKey) {
        return service.presignDelete(objectKey);
    }

}
