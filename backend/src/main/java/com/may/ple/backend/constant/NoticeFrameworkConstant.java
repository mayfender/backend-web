package com.may.ple.backend.constant;

public enum NoticeFrameworkConstant {
	XDOC(1),
	JASPER(2);
	
	private Integer id;
	
	private NoticeFrameworkConstant(Integer id) {
		this.id = id;
	}
	
	public static NoticeFrameworkConstant findById(Integer id) {
		NoticeFrameworkConstant[] values = NoticeFrameworkConstant.values();
		for (NoticeFrameworkConstant rolesConstant : values) {
			if(rolesConstant.getId().equals(id)) 
				return rolesConstant;
		}
		return null;
	}

	public Integer getId() {
		return id;
	}
	
}
