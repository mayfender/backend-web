package com.may.ple.backend.constant;

public enum DocType {
	WORD(".docx"), EXCEL(".xlsx");
	private String ext;
	
	private DocType(String ext) {
		this.ext = ext;
	}
	
	public String getExt() {
		return ext;
	}
	
}
