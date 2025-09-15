package com.ey.service;

import com.ey.dto.ApplicationResponse;
import com.ey.dto.UpdateStatusRequest;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ApplicationService {
	ApplicationResponse applyToJob(Long jobId); // existing

	ApplicationResponse applyToJob(Long jobId, MultipartFile resumeFile); // new

	List<ApplicationResponse> getMyApplications();

	List<ApplicationResponse> getApplicationsForJob(Long jobId);

	ApplicationResponse updateStatus(Long applicationId, UpdateStatusRequest request);

	Resource getResumeFile(Long applicationId);
}