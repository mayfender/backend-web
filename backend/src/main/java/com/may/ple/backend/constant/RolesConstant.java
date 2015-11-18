package com.may.ple.backend.constant;

public enum RolesConstant {
	ROLE_CUSTOMER(1, "Customer"),
	ROLE_SALE(2, "Sale"),
	ROLE_SALE_ADMIN(3, "Sale Admin"),
	ROLE_SALE_MANAGER(4, "Sale Manager"),
	ROLE_ADMIN(5, "Admin");
	
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
