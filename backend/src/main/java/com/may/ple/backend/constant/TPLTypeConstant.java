package com.may.ple.backend.constant;

public enum TPLTypeConstant {
	PRINTING(1),
	EXPORT(2),
	FORECAST(3),
	PAYMENT(4),
	LETTER(5);
	
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
