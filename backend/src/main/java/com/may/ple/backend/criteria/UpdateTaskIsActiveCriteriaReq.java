package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.model.IsActiveModel;

public class UpdateTaskIsActiveCriteriaReq extends CommonCriteriaResp {
	private String id;
	private String productId;
	private List<IsActiveModel> isActives;
	private String taskFileId;
	
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

	public String getTaskFileId() {
		return taskFileId;
	}

	public void setTaskFileId(String taskFileId) {
		this.taskFileId = taskFileId;
	}

	public List<IsActiveModel> getIsActives() {
		return isActives;
	}

	public void setIsActives(List<IsActiveModel> isActives) {
		this.isActives = isActives;
	}

}
