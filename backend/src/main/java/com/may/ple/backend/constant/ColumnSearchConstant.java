package com.may.ple.backend.constant;

public enum ColumnSearchConstant {
	Others(1, "Others"),
	OWNER(2, "sys_owner");
	
	private int id;
	private String name;
	
	private ColumnSearchConstant(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public static ColumnSearchConstant findById(int id) {
		ColumnSearchConstant[] values = ColumnSearchConstant.values();
		for (ColumnSearchConstant rolesConstant : values) {
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
