package com.may.ple.backend.constant;

public enum FileTypeConstant {
	XLSX(1, "xlsx"),
	XLS(2, "xls"),
	TXT(3, "txt");
	
	private int id;
	private String name;
	
	private FileTypeConstant(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public static FileTypeConstant findById(int id) {
		FileTypeConstant[] values = FileTypeConstant.values();
		for (FileTypeConstant rolesConstant : values) {
			if(rolesConstant.getId() == id) 
				return rolesConstant;
		}
		return null;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
}
