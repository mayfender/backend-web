package com.may.ple.backend.constant;

public enum SysFieldConstant {
	SYS_OWNER("sys_owner"),
	SYS_OLD_ORDER("sys_oldOrder"),
	SYS_IS_ACTIVE("sys_isActive"),
	SYS_FILE_ID("sys_fileId");
	
	private String name;
	
	private SysFieldConstant(String name) {
		this.name = name;
	}
	
	public static SysFieldConstant findByName(String name) {
		SysFieldConstant[] values = SysFieldConstant.values();
		for (SysFieldConstant rolesConstant : values) {
			if(rolesConstant.getName().equals(name)) 
				return rolesConstant;
		}
		return null;
	}

	public String getName() {
		return name;
	}
	
}
