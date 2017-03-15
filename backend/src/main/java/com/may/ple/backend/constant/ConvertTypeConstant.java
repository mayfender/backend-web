package com.may.ple.backend.constant;

public enum ConvertTypeConstant {
	ELS_TXT(1),
	PDF_JPG(2);
	
	private int id;
	
	private ConvertTypeConstant(int id) {
		this.id = id;
	}
	
	public static ConvertTypeConstant findById(int id) {
		ConvertTypeConstant[] values = ConvertTypeConstant.values();
		for (ConvertTypeConstant rolesConstant : values) {
			if(rolesConstant.getId() == id) 
				return rolesConstant;
		}
		return null;
	}

	public int getId() {
		return id;
	}
	
}
