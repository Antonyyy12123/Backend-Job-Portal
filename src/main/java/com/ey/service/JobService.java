package com.ey.service;
 
import com.ey.dto.JobCreateRequest;
import com.ey.dto.JobResponse;
import java.util.List;
 
public interface JobService {
    JobResponse createJob(JobCreateRequest request);
    List<JobResponse> getApprovedJobs();
    List<JobResponse> getJobsForCurrentHr();
}