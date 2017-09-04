package com.may.ple.backend.constant;

public enum SplitterConstant {
	NONE(0),
	PIPE(1),
	SPACE(2);
	
	private int id;
	
	private SplitterConstant(int id) {
		this.id = id;
	}
	
	public static SplitterConstant findById(int id) {
		SplitterConstant[] values = SplitterConstant.values();
		for (SplitterConstant rolesConstant : values) {
			if(rolesConstant.getId() == id) 
				return rolesConstant;
		}
		return null;
	}

	public int getId() {
		return id;
	}
	
}
