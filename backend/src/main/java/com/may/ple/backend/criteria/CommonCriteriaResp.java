package com.may.ple.backend.criteria;

public abstract class CommonCriteriaResp {
	private int statusCode = 9999;
	
	public CommonCriteriaResp(){}
	
	public CommonCriteriaResp(int statusCode) {
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}
