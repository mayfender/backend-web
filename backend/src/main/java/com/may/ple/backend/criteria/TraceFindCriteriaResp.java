package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TraceFindCriteriaResp extends CommonCriteriaResp {
	private List<Map> traceWorks;
	private Long totalItems;
	private String contractNo;
	private String idCardNo;
	
	public TraceFindCriteriaResp(){}
	
	public TraceFindCriteriaResp(int statusCode) {
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

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public String getIdCardNo() {
		return idCardNo;
	}

	public void setIdCardNo(String idCardNo) {
		this.idCardNo = idCardNo;
	}

	public List<Map> getTraceWorks() {
		return traceWorks;
	}

	public void setTraceWorks(List<Map> traceWorks) {
		this.traceWorks = traceWorks;
	}

}
