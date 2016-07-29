package com.may.ple.backend.constant;

public enum CompareDateStatusConstant {
	NORMAL(0),
	OVER_DATE(1),
	TODAY_APPOINT_DATE(2),
	TODAY_NEXT_TIME_DATE(3);
	
	private int status;
	
	private CompareDateStatusConstant(int status) {
		this.status = status;
	}
	
	public static CompareDateStatusConstant findById(int status) {
		CompareDateStatusConstant[] values = CompareDateStatusConstant.values();
		for (CompareDateStatusConstant rolesConstant : values) {
			if(rolesConstant.getStatus() == status) 
				return rolesConstant;
		}
		return null;
	}

	public int getStatus() {
		return status;
	}
	
}
