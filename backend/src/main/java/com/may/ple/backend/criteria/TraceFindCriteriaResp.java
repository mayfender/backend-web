package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.TraceWork;

public class TraceFindCriteriaResp extends CommonCriteriaResp {
	private List<TraceWork> traceWorks;
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

	public List<TraceWork> getTraceWorks() {
		return traceWorks;
	}

	public void setTraceWorks(List<TraceWork> traceWorks) {
		this.traceWorks = traceWorks;
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

}
