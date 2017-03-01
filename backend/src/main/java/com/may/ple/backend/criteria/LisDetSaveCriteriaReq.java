package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class LisDetSaveCriteriaReq {
	private String id;
	private String code;
	private String desc;
	private String meaning;
	private Boolean isPrintNotice;
	private Integer enabled;
	private String groupId;
	private String productId;
	private String dymListId;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getMeaning() {
		return meaning;
	}

	public void setMeaning(String meaning) {
		this.meaning = meaning;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getEnabled() {
		return enabled;
	}

	public void setEnabled(Integer enabled) {
		this.enabled = enabled;
	}

	public Boolean getIsPrintNotice() {
		return isPrintNotice;
	}

	public void setIsPrintNotice(Boolean isPrintNotice) {
		this.isPrintNotice = isPrintNotice;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getDymListId() {
		return dymListId;
	}

	public void setDymListId(String dymListId) {
		this.dymListId = dymListId;
	}

}
