package com.ey.service;

import com.ey.dto.*;
import com.ey.entity.*;
import com.ey.exception.*;
import com.ey.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterviewServiceImpl implements InterviewService {

	private final InterviewRepository interviewRepo;
	private final ApplicationRepository appRepo;
	private final UserRepository userRepo;

	public InterviewServiceImpl(InterviewRepository interviewRepo, ApplicationRepository appRepo,
			UserRepository userRepo) {
		this.interviewRepo = interviewRepo;
		this.appRepo = appRepo;
		this.userRepo = userRepo;
	}

	@Override
	@Transactional
	public InterviewResponse schedule(Long applicationId, InterviewRequest request) {
		Application app = appRepo.findById(applicationId)
				.orElseThrow(() -> new NotFoundException("Application not found"));
		String email = (String) SecurityContextHolder.getContext().getAuthentication().getName();
		User current = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

		if (current.getRole() != Role.HR && current.getRole() != Role.ADMIN) {
			throw new ForbiddenException("Only HR or Admin can schedule interviews");
		}

		if (current.getRole() == Role.HR && !app.getJob().getHr().getId().equals(current.getId())) {
			throw new ForbiddenException("Not owner of this job");
		}

		Interview iv = new Interview();
		iv.setApplication(app);
		iv.setScheduledAt(request.getScheduledAt());
		iv.setDurationMinutes(request.getDurationMinutes());
		iv.setMode(InterviewMode.valueOf(request.getMode()));
		iv.setLocation(request.getLocation());
		iv.setNotes(request.getNotes());
		iv.setCreatedBy(current);
		iv.setInterviewer(current); // default interviewer
		Interview saved = interviewRepo.save(iv);

		return toDto(saved);
	}

	@Override
	@Transactional
	public InterviewResponse reschedule(Long interviewId, InterviewRequest request) {
		Interview iv = interviewRepo.findById(interviewId)
				.orElseThrow(() -> new NotFoundException("Interview not found"));
		String email = (String) SecurityContextHolder.getContext().getAuthentication().getName();
		User current = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

		if (current.getRole() != Role.HR && current.getRole() != Role.ADMIN) {
			throw new ForbiddenException("Only HR or Admin can reschedule interviews");
		}
		if (current.getRole() == Role.HR && !iv.getApplication().getJob().getHr().getId().equals(current.getId())) {
			throw new ForbiddenException("Not owner of this job");
		}

		iv.setScheduledAt(request.getScheduledAt());
		iv.setNotes(request.getNotes());
		iv.setStatus(InterviewStatus.RESCHEDULED);
		Interview saved = interviewRepo.save(iv);
		return toDto(saved);
	}

	@Override
	public List<InterviewResponse> getMyInterviews() {
		String email = (String) SecurityContextHolder.getContext().getAuthentication().getName();
		User current = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
		List<Interview> list;
		if (current.getRole() == Role.HR) {
			list = interviewRepo.findByApplicationJobHrId(current.getId());
		} else {

			list = interviewRepo.findByApplicationIdIn(appRepo.findBySeekerId(current.getId()).stream()
					.map(Application::getId).collect(Collectors.toList()));
		}
		return list.stream().map(this::toDto).collect(Collectors.toList());
	}

	private InterviewResponse toDto(Interview iv) {
		InterviewResponse r = new InterviewResponse();
		r.setId(iv.getId());
		r.setApplicationId(iv.getApplication().getId());
		r.setJobTitle(iv.getApplication().getJob().getTitle());
		r.setScheduledAt(iv.getScheduledAt());
		r.setDurationMinutes(iv.getDurationMinutes());
		r.setMode(iv.getMode() != null ? iv.getMode().name() : null);
		r.setStatus(iv.getStatus().name());
		return r;
	}
}