package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class NoticeFindCriteriaReq extends CommonCriteriaResp {
	private String id;
	private Integer currentPage;
	private Integer itemsPerPage;
	private String productId;
	private Boolean isInit;
	private Boolean enabled;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Integer getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public Integer getItemsPerPage() {
		return itemsPerPage;
	}

	public void setItemsPerPage(Integer itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

	public Boolean getIsInit() {
		return isInit;
	}

	public void setIsInit(Boolean isInit) {
		this.isInit = isInit;
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

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

}
