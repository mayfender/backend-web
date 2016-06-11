package com.may.ple.backend.entity;

public class IsActive {
	private Boolean status;
	private String reason;
	
	public IsActive(Boolean status, String reason) {
		this.status = status;
		this.reason = reason;
	}
	public Boolean getStatus() {
		return status;
	}
	public void setStatus(Boolean status) {
		this.status = status;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
}
