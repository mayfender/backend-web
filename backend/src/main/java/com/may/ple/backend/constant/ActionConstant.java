package com.may.ple.backend.constant;

public enum ActionConstant {
	UPDATED("UPDATED"),
	DELETED("DELETED");
	
	private String name;
	
	private ActionConstant(String name) {
		this.name = name;
	}
	
	public static ActionConstant findByName(String name) {
		ActionConstant[] values = ActionConstant.values();
		for (ActionConstant rolesConstant : values) {
			if(rolesConstant.getName().equals(name)) 
				return rolesConstant;
		}
		return null;
	}

	public String getName() {
		return name;
	}
	
}
