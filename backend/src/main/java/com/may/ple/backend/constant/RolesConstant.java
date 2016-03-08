package com.may.ple.backend.constant;

public enum RolesConstant {
	ROLE_GUEST(1, "Guest"),
	ROLE_MEMBER(2, "Member"),
	ROLE_USER(3, "User"), 
	ROLE_ADMIN(4, "Admin");
	
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
