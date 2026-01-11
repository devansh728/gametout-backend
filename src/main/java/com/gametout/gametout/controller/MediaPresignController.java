package com.gametout.gametout.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
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
        return service.presignUpload(filename, contentType);
    }

    @PostMapping("/resume")
    @EmailVerifiedRequired
    public Map<String, String> presignResume(
            @RequestParam String filename) {
        return service.presignUpload(
                filename, "application/pdf");
    }

}
