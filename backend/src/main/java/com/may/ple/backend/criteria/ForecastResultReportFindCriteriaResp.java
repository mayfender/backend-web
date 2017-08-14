package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ForecastResultReportFile;

public class ForecastResultReportFindCriteriaResp extends CommonCriteriaResp {
	private List<ForecastResultReportFile> files;
	private Long totalItems;
	
	public ForecastResultReportFindCriteriaResp(){}
	
	public ForecastResultReportFindCriteriaResp(int statusCode) {
		super(statusCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getTotalItems() {
		return totalItems;
	}
	
	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}

	public List<ForecastResultReportFile> getFiles() {
		return files;
	}

	public void setFiles(List<ForecastResultReportFile> files) {
		this.files = files;
	}

}
