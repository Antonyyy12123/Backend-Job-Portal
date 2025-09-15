package com.ey.controller;

import com.ey.dto.*;
import com.ey.service.InterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {

	private final InterviewService interviewService;

	public InterviewController(InterviewService interviewService) {
		this.interviewService = interviewService;
	}

	@PostMapping("/applications/{applicationId}")
	@PreAuthorize("hasRole('HR')")
	public ResponseEntity<?> schedule(@PathVariable Long applicationId, @Valid @RequestBody InterviewRequest request) {
		return ResponseEntity.status(201).body(interviewService.schedule(applicationId, request));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('HR')")
	public ResponseEntity<?> reschedule(@PathVariable Long id, @Valid @RequestBody InterviewRequest request) {
		return ResponseEntity.ok(interviewService.reschedule(id, request));
	}

	@GetMapping("/my")
	public ResponseEntity<?> myInterviews() {
		return ResponseEntity.ok(interviewService.getMyInterviews());
	}
	
}
