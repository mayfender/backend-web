package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.model.IsHoldModel;

public class UpdateTraceResultCriteriaReq extends CommonCriteriaResp {
	private String id;
	private String productId;
	private List<IsHoldModel> isHolds;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public List<IsHoldModel> getIsHolds() {
		return isHolds;
	}

	public void setIsHolds(List<IsHoldModel> isHolds) {
		this.isHolds = isHolds;
	}

}
