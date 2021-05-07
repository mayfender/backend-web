package com.may.ple.backend.constant;

public enum SysFieldConstant {
	SYS_OWNER("sys_owner"),
	SYS_OWNER_SHOWNAME("sys_owner_showname"),
	SYS_OWNER_FIRST_NAME("sys_owner_first_name"),
	SYS_OWNER_LAST_NAME("sys_owner_last_name"),
	SYS_OWNER_FIRST_NAME_ENG("sys_owner_first_name_eng"),
	SYS_OWNER_LAST_NAME_ENG("sys_owner_last_name_eng"),
	SYS_OWNER_FULL_NAME("sys_owner_full_name"),
	SYS_OWNER_FULL_NAME_ENG("sys_owner_full_name_eng"),
	SYS_CREATED_SHOWNAME("sys_created_showname"),
	SYS_CREATED_FIRST_NAME("sys_created_first_name"),
	SYS_CREATED_FIRST_NAME_ENG("sys_created_first_name_eng"),
	SYS_CREATED_LAST_NAME("sys_created_last_name"),
	SYS_CREATED_LAST_NAME_ENG("sys_created_last_name_eng"),
	SYS_CREATED_FULL_NAME("sys_created_full_name"),
	SYS_CREATED_FULL_NAME_ENG("sys_created_full_name_eng"),
	SYS_OWNER_ID("sys_owner_id"),
	SYS_PROBATION_OWNER_ID("sys_probation_owner_id"),
	SYS_OLD_ORDER("sys_oldOrder"),
	SYS_IS_ACTIVE("sys_isActive"),
	SYS_FILE_ID("sys_fileId"),
	SYS_APPOINT_DATE("sys_appointDate"),
	SYS_APPOINT_AMOUNT("sys_appointAmount"),
	SYS_NEXT_TIME_DATE("sys_nextTimeDate"),
	SYS_TRACE_DATE("sys_traceDate"),
	SYS_RESULT_TEXT("sys_resultText"),
	SYS_TEL("sys_tel"),
	SYS_COMPARE_DATE_STATUS("sys_compareDateStatus"),
	SYS_CREATED_DATE_TIME("sys_createdDateTime"),
	SYS_UPDATED_DATE_TIME("sys_updatedDateTime"),
	SYS_TAGS("sys_tags"),
	SYS_TAGS_U("sys_tags_u"),
	SYS_NOW_DATETIME("sys_now_datetime"),
	SYS_COUNT("sys_count");

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
