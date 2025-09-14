package com.ey.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateStatusRequest {
    @NotBlank(message = "status is required (APPLIED, REVIEWED, ACCEPTED, REJECTED)")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
