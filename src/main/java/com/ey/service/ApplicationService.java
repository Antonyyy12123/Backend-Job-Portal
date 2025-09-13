package com.ey.service;
 
import com.ey.dto.ApplicationResponse;
import com.ey.dto.UpdateStatusRequest;
import java.util.List;
 
public interface ApplicationService {
    ApplicationResponse applyToJob(Long jobId);
    List<ApplicationResponse> getMyApplications();
    List<ApplicationResponse> getApplicationsForJob(Long jobId);
    ApplicationResponse updateStatus(Long applicationId, UpdateStatusRequest request);
}