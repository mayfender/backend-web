package com.may.ple.backend.constant;

public enum CollectNameConstant {
	NEW_TASK_DETAIL("newTaskDetail");
	
	private String name;
	
	private CollectNameConstant(String name) {
		this.name = name;
	}
	
	public static CollectNameConstant findByName(String name) {
		CollectNameConstant[] values = CollectNameConstant.values();
		for (CollectNameConstant rolesConstant : values) {
			if(rolesConstant.getName().equals(name)) 
				return rolesConstant;
		}
		return null;
	}

	public String getName() {
		return name;
	}
	
}
