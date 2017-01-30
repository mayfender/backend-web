package com.may.ple.backend.constant;

public enum SysFieldConstant {
	SYS_OWNER("sys_owner"),
	SYS_OWNER_ID("sys_owner_id"),
	SYS_OLD_ORDER("sys_oldOrder"),
	SYS_IS_ACTIVE("sys_isActive"),
	SYS_FILE_ID("sys_fileId"),
	SYS_APPOINT_DATE("sys_appointDate"),
	SYS_NEXT_TIME_DATE("sys_nextTimeDate"),
	SYS_COMPARE_DATE_STATUS("sys_compareDateStatus"),
	SYS_CREATED_DATE_TIME("sys_createdDateTime"),
	SYS_UPDATED_DATE_TIME("sys_updatedDateTime"),
	SYS_TAGS("sys_tags");
	
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
