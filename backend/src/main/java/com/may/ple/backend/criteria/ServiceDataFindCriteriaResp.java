package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.ServiceData;

public class ServiceDataFindCriteriaResp extends CommonCriteriaResp {
	private List<ServiceData> serviceDatas;
	
	public ServiceDataFindCriteriaResp() {}
	
	public ServiceDataFindCriteriaResp(int statusCode) {
		super(statusCode);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<ServiceData> getServiceDatas() {
		return serviceDatas;
	}
	public void setServiceDatas(List<ServiceData> serviceDatas) {
		this.serviceDatas = serviceDatas;
	}

}
