package com.ey.service;
 
import com.ey.dto.ApplicationResponse;
import com.ey.dto.UpdateStatusRequest;
import com.ey.entity.Application;
import com.ey.entity.ApplicationStatus;
import com.ey.entity.Job;
import com.ey.entity.Role;
import com.ey.entity.User;
import com.ey.exception.BadRequestException;
import com.ey.exception.ConflictException;
import com.ey.exception.ForbiddenException;
import com.ey.exception.NotFoundException;
import com.ey.repository.ApplicationRepository;
import com.ey.repository.JobRepository;
import com.ey.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
 
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
 
@Service
public class ApplicationServiceImpl implements ApplicationService {
 
    private final ApplicationRepository appRepo;
    private final JobRepository jobRepo;
    private final UserRepository userRepo;
    private final Path uploadRoot;
 
    public ApplicationServiceImpl(ApplicationRepository appRepo,
                                  JobRepository jobRepo,
                                  UserRepository userRepo,
                                  // read from property 'file.upload-dir' or default to 'uploads'
                                  @org.springframework.beans.factory.annotation.Value("${file.upload-dir:uploads}") String uploadDir) {
        this.appRepo = appRepo;
        this.jobRepo = jobRepo;
        this.userRepo = userRepo;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }
 
    @Override
    @Transactional
    public ApplicationResponse applyToJob(Long jobId) {
        return applyToJob(jobId, null);
    }
 
    @Override
    @Transactional
    public ApplicationResponse applyToJob(Long jobId, MultipartFile resumeFile) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getName();
        User seeker = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        Job job = jobRepo.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found"));
 
        if (appRepo.existsByJobIdAndSeekerId(jobId, seeker.getId())) {
            throw new ConflictException("Already applied to this job");
        }
 
        Application app = new Application();
        app.setJob(job);
        app.setSeeker(seeker);
 
        // Handle resume file if provided
        if (resumeFile != null && !resumeFile.isEmpty()) {
            // Validate content type and extension
            String contentType = resumeFile.getContentType();
            String originalFilename = resumeFile.getOriginalFilename() != null ? resumeFile.getOriginalFilename() : "";
            if (!isPdf(contentType, originalFilename)) {
                throw new BadRequestException("Only PDF resumes are allowed");
            }
 
            // Ensure upload directory exists
            try {
                Files.createDirectories(uploadRoot);
            } catch (IOException e) {
                throw new RuntimeException("Could not create upload directory", e);
            }
 
            // Save file with random UUID name to avoid collisions
            String filename = UUID.randomUUID().toString() + ".pdf";
            Path destination = uploadRoot.resolve(filename).normalize();
            try {
                // copy (replace if exists just in case)
                Files.copy(resumeFile.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                app.setResumePath(destination.toString());
            } catch (IOException e) {
                throw new RuntimeException("Failed to store resume file", e);
            }
        }
 
        Application saved = appRepo.save(app);
        return toDto(saved);
    }
 
    private boolean isPdf(String contentType, String filename) {
        if (contentType != null && contentType.equalsIgnoreCase("application/pdf")) return true;
        String lower = filename.toLowerCase();
        return lower.endsWith(".pdf");
    }
 
    @Override
    public List<ApplicationResponse> getMyApplications() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getName();
        User seeker = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        return appRepo.findBySeekerId(seeker.getId()).stream().map(this::toDto).collect(Collectors.toList());
    }
 
    @Override
    public List<ApplicationResponse> getApplicationsForJob(Long jobId) {
        Job job = jobRepo.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found"));
        return appRepo.findByJobId(jobId).stream().map(this::toDto).collect(Collectors.toList());
    }
 
    @Override
    @Transactional
    public ApplicationResponse updateStatus(Long applicationId, UpdateStatusRequest request) {
        Application app = appRepo.findById(applicationId).orElseThrow(() -> new NotFoundException("Application not found"));
        // ownership: check HR owns the job
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getName();
        User current = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        if (current.getRole() != Role.HR) throw new ForbiddenException("Only HR can update application status");
        if (!app.getJob().getHr().getId().equals(current.getId())) throw new ForbiddenException("Not owner of this job");
 
        app.setStatus(ApplicationStatus.valueOf(request.getStatus()));
        Application saved = appRepo.save(app);
        return toDto(saved);
    }
 
    @Override
    public org.springframework.core.io.Resource getResumeFile(Long applicationId) {
        Application app = appRepo.findById(applicationId).orElseThrow(() -> new NotFoundException("Application not found"));
        if (app.getResumePath() == null || app.getResumePath().isBlank()) {
            throw new NotFoundException("Resume not found for this application");
        }
 
        Path filePath = Paths.get(app.getResumePath()).toAbsolutePath().normalize();
 
        // Basic safety: ensure filePath is inside uploadRoot
        if (!filePath.startsWith(this.uploadRoot)) {
            throw new ForbiddenException("Access to this file is forbidden");
        }
 
        try {
            java.net.URL url = filePath.toUri().toURL();
            UrlResource resource = new UrlResource(url);
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new NotFoundException("Resume file not found");
            }
        } catch (MalformedURLException e) {
            throw new NotFoundException("Resume file not found");
        }
    }
 
    private ApplicationResponse toDto(Application a) {
        ApplicationResponse r = new ApplicationResponse();
        r.setId(a.getId());
        r.setJobTitle(a.getJob().getTitle());
        r.setSeekerName(a.getSeeker().getName());
        r.setStatus(a.getStatus().name());
        r.setAppliedAt(a.getAppliedAt());
        return r;
    }
}