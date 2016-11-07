package com.may.ple.backend.criteria;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TraceResultReportFindCriteriaReq extends CommonCriteriaResp {
	private String id;
	private Integer currentPage;
	private Integer itemsPerPage;
	private String productId;
	private Boolean enabled;
	private Boolean isFillTemplate;
	private String columnName;
	private String order;
	private String keyword;
	private String owner;
	private String dateColumnName;
	private Date dateFrom;
	private Date dateTo;
	private String actionCodeId;
	private String resultCodeId;
	private Integer exportType;
	
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

	public Boolean getIsFillTemplate() {
		return isFillTemplate;
	}

	public void setIsFillTemplate(Boolean isFillTemplate) {
		this.isFillTemplate = isFillTemplate;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getDateColumnName() {
		return dateColumnName;
	}

	public void setDateColumnName(String dateColumnName) {
		this.dateColumnName = dateColumnName;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public String getActionCodeId() {
		return actionCodeId;
	}

	public void setActionCodeId(String actionCodeId) {
		this.actionCodeId = actionCodeId;
	}

	public String getResultCodeId() {
		return resultCodeId;
	}

	public void setResultCodeId(String resultCodeId) {
		this.resultCodeId = resultCodeId;
	}

	public Integer getExportType() {
		return exportType;
	}

	public void setExportType(Integer exportType) {
		this.exportType = exportType;
	}

}
