package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;

public class GetColumnFormatsCriteriaResp extends CommonCriteriaResp {
	private List<ColumnFormat> columnFormats;
	private List<ColumnFormat> mainColumnFormats;
	
	public GetColumnFormatsCriteriaResp(){}
	
	public GetColumnFormatsCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<ColumnFormat> getColumnFormats() {
		return columnFormats;
	}

	public void setColumnFormats(List<ColumnFormat> columnFormats) {
		this.columnFormats = columnFormats;
	}

	public List<ColumnFormat> getMainColumnFormats() {
		return mainColumnFormats;
	}

	public void setMainColumnFormats(List<ColumnFormat> mainColumnFormats) {
		this.mainColumnFormats = mainColumnFormats;
	}
	
}
