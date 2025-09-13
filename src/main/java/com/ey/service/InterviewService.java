package com.ey.service;
 
import com.ey.dto.InterviewRequest;
import com.ey.dto.InterviewResponse;
import java.util.List;
 
public interface InterviewService {
    InterviewResponse schedule(Long applicationId, InterviewRequest request);
    InterviewResponse reschedule(Long interviewId, InterviewRequest request);
    List<InterviewResponse> getMyInterviews();
}