package com.ey.controller;
 
import com.ey.dto.ApplicationResponse;
import com.ey.dto.JobCreateRequest;
import com.ey.service.ApplicationService;
import com.ey.service.JobService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
 
@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {
 
    private final JobService jobService;
    private final ApplicationService applicationService;
 
    public JobController(JobService jobService, ApplicationService applicationService) {
        this.jobService = jobService;
        this.applicationService = applicationService;
    }
 
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
 
    @PostMapping("/{jobId}/apply")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<ApplicationResponse> applyToJob(
            @PathVariable Long jobId,
            @RequestPart(required = false) MultipartFile resume) {
        return ResponseEntity.status(201).body(applicationService.applyToJob(jobId, resume));
    }
 
    @GetMapping("/{jobId}/applications/{applicationId}/resume")
    @PreAuthorize("hasAnyRole('HR','ADMIN','SEEKER')")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long jobId,
                                                   @PathVariable Long applicationId) {
        Resource resource = applicationService.getResumeFile(applicationId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
 