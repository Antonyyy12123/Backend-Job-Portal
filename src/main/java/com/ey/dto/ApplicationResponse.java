package com.ey.dto;

import java.time.Instant;

public class ApplicationResponse {

	private Long id;
	private String jobTitle;
	private String seekerName;
	private String status;
	private Instant appliedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getSeekerName() {
		return seekerName;
	}

	public void setSeekerName(String seekerName) {
		this.seekerName = seekerName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Instant getAppliedAt() {
		return appliedAt;
	}

	public void setAppliedAt(Instant appliedAt) {
		this.appliedAt = appliedAt;
	}

}
