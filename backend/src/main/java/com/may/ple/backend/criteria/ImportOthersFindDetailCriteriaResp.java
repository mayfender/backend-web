package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ColumnFormat;

public class ImportOthersFindDetailCriteriaResp extends CommonCriteriaResp {
	private List<Map> dataLst;
	private List<ColumnFormat> headers;
	private Long totalItems;
	
	public ImportOthersFindDetailCriteriaResp(){}
	
	public ImportOthersFindDetailCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<Map> getDataLst() {
		return dataLst;
	}

	public void setDataLst(List<Map> dataLst) {
		this.dataLst = dataLst;
	}

	public List<ColumnFormat> getHeaders() {
		return headers;
	}

	public void setHeaders(List<ColumnFormat> headers) {
		this.headers = headers;
	}

	public Long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

}
