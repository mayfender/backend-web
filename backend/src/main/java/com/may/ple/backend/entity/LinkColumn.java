package com.may.ple.backend.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class LinkColumn {
	private String mainColumn;
	private String childColumn;
	
	public LinkColumn() {}
	
	public LinkColumn(String mainColumn, String childColumn) {
		this.mainColumn = mainColumn;
		this.childColumn = childColumn;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getMainColumn() {
		return mainColumn;
	}

	public void setMainColumn(String mainColumn) {
		this.mainColumn = mainColumn;
	}

	public String getChildColumn() {
		return childColumn;
	}

	public void setChildColumn(String childColumn) {
		this.childColumn = childColumn;
	}
	
}
