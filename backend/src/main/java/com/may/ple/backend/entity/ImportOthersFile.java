package com.may.ple.backend.entity;

import java.util.Date;

public class ImportOthersFile extends NewTaskFile {
	private String menuId;
	
	public ImportOthersFile(String fileName, Date createdDateTime) {
		super(fileName, createdDateTime);
	}

	public String getMenuId() {
		return menuId;
	}

	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

}
