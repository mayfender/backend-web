package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ExportTemplateFile;
import com.may.ple.backend.entity.TraceResultReportFile;

public class ExportTemplateFindCriteriaResp extends CommonCriteriaResp {
	private List<ExportTemplateFile> files;
	private Long totalItems;
	
	public ExportTemplateFindCriteriaResp(){}
	
	public ExportTemplateFindCriteriaResp(int statusCode) {
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

	public List<ExportTemplateFile> getFiles() {
		return files;
	}

	public void setFiles(List<ExportTemplateFile> files) {
		this.files = files;
	}

}
