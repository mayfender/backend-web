package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.PaymentReportFile;

public class PaymentReportFindCriteriaResp extends CommonCriteriaResp {
	private List<PaymentReportFile> files;
	private Long totalItems;
	
	public PaymentReportFindCriteriaResp(){}
	
	public PaymentReportFindCriteriaResp(int statusCode) {
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

	public List<PaymentReportFile> getFiles() {
		return files;
	}

	public void setFiles(List<PaymentReportFile> files) {
		this.files = files;
	}

}
