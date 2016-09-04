package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.TraceResultReportFile;

public class TraceResultReportFindCriteriaResp extends CommonCriteriaResp {
	private List<TraceResultReportFile> files;
	private Long totalItems;
	
	public TraceResultReportFindCriteriaResp(){}
	
	public TraceResultReportFindCriteriaResp(int statusCode) {
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

	public List<TraceResultReportFile> getFiles() {
		return files;
	}

	public void setFiles(List<TraceResultReportFile> files) {
		this.files = files;
	}

}
