package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ServiceDataEditCriteriaResp extends CommonCriteriaResp {
	private ServiceDataSaveCriteriaReq serviceData;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public ServiceDataSaveCriteriaReq getServiceData() {
		return serviceData;
	}
	public void setServiceData(ServiceDataSaveCriteriaReq serviceData) {
		this.serviceData = serviceData;
	}

}
