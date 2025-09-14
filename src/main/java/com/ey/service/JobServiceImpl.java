package com.ey.service;

import com.ey.dto.JobCreateRequest;
import com.ey.dto.JobResponse;
import com.ey.entity.*;
import com.ey.exception.ForbiddenException;
import com.ey.exception.NotFoundException;
import com.ey.repository.CompanyRepository;
import com.ey.repository.JobRepository;
import com.ey.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepo;
    private final UserRepository userRepo;
    @SuppressWarnings("unused")
    private final CompanyRepository companyRepo;

    public JobServiceImpl(JobRepository jobRepo, UserRepository userRepo, CompanyRepository companyRepo) {
        this.jobRepo = jobRepo;
        this.userRepo = userRepo;
        this.companyRepo = companyRepo;
    }

    @Override
    @Transactional
    public JobResponse createJob(JobCreateRequest request) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getName();
        User hr = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("HR not found"));

        if (hr.getRole() != Role.HR) {
            throw new ForbiddenException("Only HR can create jobs");
        }

        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setHr(hr);
        job.setCompany(hr.getCompany());

        if (hr.getHrStatus() == HrStatus.APPROVED) {
            job.setStatus(JobStatus.APPROVED);
            job.setApprovedAt(Instant.now());
        } else {
            job.setStatus(JobStatus.PENDING);
        }

        if (request.getRequirements() != null) {
            List<JobRequirement> reqs = request.getRequirements().stream().map(r -> {
                JobRequirement jr = new JobRequirement();
                jr.setRequirement(r);
                jr.setJob(job);
                return jr;
            }).collect(Collectors.toList());
            job.setRequirements(reqs);
        }

        Job saved = jobRepo.save(job);
        return toDto(saved);
    }

    @Override
    public List<JobResponse> getApprovedJobs() {
        return jobRepo.findByStatus(JobStatus.APPROVED)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getJobsForCurrentHr() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getName();
        User hr = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("HR not found"));
        return jobRepo.findByHrIdOrderByCreatedAtDesc(hr.getId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private JobResponse toDto(Job j) {
        JobResponse r = new JobResponse();
        r.setId(j.getId());
        r.setTitle(j.getTitle());
        r.setDescription(j.getDescription());
        r.setLocation(j.getLocation());
        r.setSalary(j.getSalary());
        r.setStatus(j.getStatus() != null ? j.getStatus().name() : null);
        r.setCreatedAt(j.getCreatedAt());
        if (j.getCompany() != null) {
            r.setCompany(j.getCompany().getName());
        }

        // Null-safe handling of requirements
        if (j.getRequirements() != null) {
            r.setRequirements(
                    j.getRequirements().stream()
                            .map(JobRequirement::getRequirement)
                            .collect(Collectors.toList())
            );
        } else {
            r.setRequirements(Collections.emptyList());
        }

        return r;
    }
}
