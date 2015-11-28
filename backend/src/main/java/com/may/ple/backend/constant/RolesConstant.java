package com.may.ple.backend.constant;

public enum RolesConstant {
	ROLE_WORKER(1, "Worker"), 
	ROLE_CASHIER(2, "Cashier"), 
	ROLE_ADMIN(3, "Admin");
	
	private int id;
	private String name;
	
	private RolesConstant(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public static RolesConstant findById(int id) {
		RolesConstant[] values = RolesConstant.values();
		for (RolesConstant rolesConstant : values) {
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
