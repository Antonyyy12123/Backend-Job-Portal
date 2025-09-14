package com.ey.controller;
 
import com.ey.dto.*;
import com.ey.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {
 
    private final ApplicationService appService;
    public ApplicationController(ApplicationService appService) { this.appService = appService; }
 
    @PostMapping("/{jobId}")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<?> apply(@PathVariable Long jobId) {
        return ResponseEntity.status(201).body(appService.applyToJob(jobId));
    }
 
    @GetMapping("/mine")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<?> myApplications() {
        return ResponseEntity.ok(appService.getMyApplications());
    }
 
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<?> applicationsForJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(appService.getApplicationsForJob(jobId));
    }
 
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(appService.updateStatus(id, request));
    }
}