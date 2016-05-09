package com.may.ple.backend.constant;

public enum ExportTypeConstant {
	RECEIPT(1),
	REGISTER_FORM(2),
	REGISTER_DATA(2);
	
	private int id;
	
	private ExportTypeConstant(int id) {
		this.id = id;
	}
	
	public static ExportTypeConstant findById(int id) {
		ExportTypeConstant[] values = ExportTypeConstant.values();
		for (ExportTypeConstant rolesConstant : values) {
			if(rolesConstant.getId() == id) 
				return rolesConstant;
		}
		return null;
	}

	public int getId() {
		return id;
	}
	
}
