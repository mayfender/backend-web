package com.may.ple.backend.constant;

public enum TaskTypeConstant {
	EMPTY(1, "Empty owner"),
	TRANSFER(2, "Transfer to other owner");
	
	private int id;
	private String name;
	
	private TaskTypeConstant(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public static TaskTypeConstant findById(int id) {
		TaskTypeConstant[] values = TaskTypeConstant.values();
		for (TaskTypeConstant rolesConstant : values) {
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
