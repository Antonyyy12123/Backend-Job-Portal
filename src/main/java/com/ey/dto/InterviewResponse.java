package com.ey.dto;

import java.time.Instant;

public class InterviewResponse {
	private Long id;
	private Long applicationId;
	private String jobTitle;
	private Instant scheduledAt;
	private Integer durationMinutes;
	private String mode;
	private String status;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}
	public String getJobTitle() {
		return jobTitle;
	}
	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}
	public Instant getScheduledAt() {
		return scheduledAt;
	}
	public void setScheduledAt(Instant scheduledAt) {
		this.scheduledAt = scheduledAt;
	}
	public Integer getDurationMinutes() {
		return durationMinutes;
	}
	public void setDurationMinutes(Integer durationMinutes) {
		this.durationMinutes = durationMinutes;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	

}
