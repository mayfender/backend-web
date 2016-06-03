package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumFormat;

public class GetColumnFormatsCriteriaResp extends CommonCriteriaResp {
	private List<ColumFormat> columnFormats;
	
	public GetColumnFormatsCriteriaResp(){}
	
	public GetColumnFormatsCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<ColumFormat> getColumnFormats() {
		return columnFormats;
	}

	public void setColumnFormats(List<ColumFormat> columnFormats) {
		this.columnFormats = columnFormats;
	}
	
}
