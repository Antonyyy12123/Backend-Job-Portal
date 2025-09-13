package com.ey.controller;
 
import com.ey.dto.*;
import com.ey.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {
 
    private final JobService jobService;
    public JobController(JobService jobService) { this.jobService = jobService; }
 
    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<?> createJob(@RequestBody JobCreateRequest request) {
        return ResponseEntity.status(201).body(jobService.createJob(request));
    }
 
    @GetMapping
    public ResponseEntity<?> listPublicJobs() {
        return ResponseEntity.ok(jobService.getApprovedJobs());
    }
 
    @GetMapping("/my")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<?> listMyJobs() {
        return ResponseEntity.ok(jobService.getJobsForCurrentHr());
    }
}
 