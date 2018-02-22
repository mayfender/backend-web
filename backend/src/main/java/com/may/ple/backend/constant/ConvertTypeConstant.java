package com.may.ple.backend.constant;

public enum ConvertTypeConstant {
	XLS_TXT(1, "txt"),
	TO_JPG(2, "jpg"),
	WEB_XLS(3, "xlsx");
	
	private int id;
	private String ext;
	
	private ConvertTypeConstant(int id, String ext) {
		this.id = id;
		this.ext = ext;
	}
	
	public static ConvertTypeConstant findById(int id) {
		ConvertTypeConstant[] values = ConvertTypeConstant.values();
		for (ConvertTypeConstant rolesConstant : values) {
			if(rolesConstant.getId() == id) 
				return rolesConstant;
		}
		return null;
	}
	
	public static ConvertTypeConstant findByExt(String ext) {
		ConvertTypeConstant[] values = ConvertTypeConstant.values();
		for (ConvertTypeConstant rolesConstant : values) {
			if(rolesConstant.getExt().equals(ext)) 
				return rolesConstant;
		}
		return null;
	}

	public int getId() {
		return id;
	}
	
	public String getExt() {
		return ext;
	}
	
}
