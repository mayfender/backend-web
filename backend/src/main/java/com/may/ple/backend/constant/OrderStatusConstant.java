package com.may.ple.backend.constant;

public enum OrderStatusConstant {
	QUEUED(0), 
	DOING(1), 
	FINISHED(2);
	
	private int statusNo;
	
	private OrderStatusConstant(int statusNo) {
		this.statusNo = statusNo;
	}
	
	public static OrderStatusConstant findByStatusNo(int statusNo) {
		OrderStatusConstant[] values = OrderStatusConstant.values();
		for (OrderStatusConstant rolesConstant : values) {
			if(rolesConstant.getStatusNo() == statusNo) 
				return rolesConstant;
		}
		return null;
	}

	public int getStatusNo() {
		return statusNo;
	}
	
}
