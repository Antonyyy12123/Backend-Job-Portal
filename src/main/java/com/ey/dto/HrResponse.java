package com.ey.dto;

public class HrResponse {
	private Long id;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getHrStatus() {
		return hrStatus;
	}
	public void setHrStatus(String hrStatus) {
		this.hrStatus = hrStatus;
	}
	private String name;
	private String email;
	private String company;
	private String hrStatus;

}
