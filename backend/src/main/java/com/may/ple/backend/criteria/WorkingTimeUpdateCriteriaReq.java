package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class WorkingTimeUpdateCriteriaReq {
	private String productId;
	private Integer startTimeH;
	private Integer startTimeM;
	private Integer endTimeH;
	private Integer endTimeM;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public Integer getStartTimeH() {
		return startTimeH;
	}

	public void setStartTimeH(Integer startTimeH) {
		this.startTimeH = startTimeH;
	}

	public Integer getStartTimeM() {
		return startTimeM;
	}

	public void setStartTimeM(Integer startTimeM) {
		this.startTimeM = startTimeM;
	}

	public Integer getEndTimeH() {
		return endTimeH;
	}

	public void setEndTimeH(Integer endTimeH) {
		this.endTimeH = endTimeH;
	}

	public Integer getEndTimeM() {
		return endTimeM;
	}

	public void setEndTimeM(Integer endTimeM) {
		this.endTimeM = endTimeM;
	}

}
