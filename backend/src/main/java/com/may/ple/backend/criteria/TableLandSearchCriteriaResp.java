package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.TableLand;

public class TableLandSearchCriteriaResp extends CommonCriteriaResp {
	private List<TableLand> tables;
	
	public TableLandSearchCriteriaResp() {}
	
	public TableLandSearchCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
	
	public List<TableLand> getTables() {
		return tables;
	}
	public void setTables(List<TableLand> tables) {
		this.tables = tables;
	}

}
