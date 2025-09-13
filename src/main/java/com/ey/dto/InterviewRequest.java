package com.ey.dto;

import java.time.Instant;

public class InterviewRequest {
	private Instant scheduledAt;
	private Integer  durationMinutes;
	private String mode;
	private String location;
	private String notes;
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
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	

}
