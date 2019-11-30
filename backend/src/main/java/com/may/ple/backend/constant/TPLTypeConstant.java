package com.may.ple.backend.constant;

public enum TPLTypeConstant {
	ACC(1),
	FORECAST(2),
	TRACE(3),
	PAYMENT(4),
	LETTER(5),
	PRINTING(6),
	SMS(7),
	RECEIPT(8);
	
	private int id;
	
	private TPLTypeConstant(int id) {
		this.id = id;
	}
	
	public static TPLTypeConstant findById(int id) {
		TPLTypeConstant[] values = TPLTypeConstant.values();
		for (TPLTypeConstant rolesConstant : values) {
			if(rolesConstant.getId() == id) 
				return rolesConstant;
		}
		return null;
	}

	public int getId() {
		return id;
	}
	
}
