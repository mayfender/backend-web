package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CodeSaveCriteriaReq {
	private String id;
	private String code;
	private String desc;
	private String meaning;
	private Boolean isPrintNotice;
	private Integer enabled;
	private String resultGroupId;
	private String productId;
	
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

	public String getResultGroupId() {
		return resultGroupId;
	}

	public void setResultGroupId(String resultGroupId) {
		this.resultGroupId = resultGroupId;
	}

	public Boolean getIsPrintNotice() {
		return isPrintNotice;
	}

	public void setIsPrintNotice(Boolean isPrintNotice) {
		this.isPrintNotice = isPrintNotice;
	}

}
