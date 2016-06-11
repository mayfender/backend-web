package com.may.ple.backend.constant;

public enum AssignMethodConstant {
	RANDOM(1, "Random"),
	PERFORMANCE(2, "Performance");
	
	private int id;
	private String name;
	
	private AssignMethodConstant(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public static AssignMethodConstant findById(int id) {
		AssignMethodConstant[] values = AssignMethodConstant.values();
		for (AssignMethodConstant rolesConstant : values) {
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
