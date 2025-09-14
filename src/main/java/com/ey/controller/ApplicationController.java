package com.ey.controller;

import com.ey.dto.*;
import com.ey.service.ApplicationService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService appService;
    public ApplicationController(ApplicationService appService) { this.appService = appService; }

    // Apply with optional resume file (Seeker only)
    @PostMapping("/{jobId}")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<?> apply(@PathVariable @Positive(message = "jobId must be positive") Long jobId,
                                   @RequestParam(value = "resume", required = false) MultipartFile resume) {
        return ResponseEntity.status(201).body(appService.applyToJob(jobId, resume));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<?> myApplications() {
        return ResponseEntity.ok(appService.getMyApplications());
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<?> applicationsForJob(@PathVariable @Positive(message = "jobId must be positive") Long jobId) {
        return ResponseEntity.ok(appService.getApplicationsForJob(jobId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(appService.updateStatus(id, request));
    }

    // Download resume for an application - HR or ADMIN only
    @GetMapping("/{applicationId}/resume")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long applicationId) {
        Resource file = appService.getResumeFile(applicationId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume-" + applicationId + ".pdf\"")
                .body(file);
    }
}
