package com.may.ple.backend.criteria;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ForecastResultCriteriaReq {
	private String id;
	private String taskFileId;
	private String productId;
	private Integer currentPage;
	private Integer itemsPerPage;
	private String columnName;
	private String order;
	private String keyword;
	private String owner;
	private String dateColumnName;
	private Date dateFrom;
	private Date dateTo;
	private Boolean isInit;
	private Boolean isFillTemplate;
	private Boolean isLastOnly;
	private Boolean isActiveOnly;
	private String codeName;
	private String codeValue;
	private String dymSearchFiedName;
	private String dymSearchFiedVal;
	
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

	public String getTaskFileId() {
		return taskFileId;
	}

	public void setTaskFileId(String taskFileId) {
		this.taskFileId = taskFileId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
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

	public Boolean getIsInit() {
		return isInit;
	}

	public void setIsInit(Boolean isInit) {
		this.isInit = isInit;
	}

	public Boolean getIsFillTemplate() {
		return isFillTemplate;
	}

	public void setIsFillTemplate(Boolean isFillTemplate) {
		this.isFillTemplate = isFillTemplate;
	}

	public Boolean getIsLastOnly() {
		return isLastOnly;
	}

	public void setIsLastOnly(Boolean isLastOnly) {
		this.isLastOnly = isLastOnly;
	}

	public Boolean getIsActiveOnly() {
		return isActiveOnly;
	}

	public void setIsActiveOnly(Boolean isActiveOnly) {
		this.isActiveOnly = isActiveOnly;
	}

	public String getCodeName() {
		return codeName;
	}

	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}

	public String getCodeValue() {
		return codeValue;
	}

	public void setCodeValue(String codeValue) {
		this.codeValue = codeValue;
	}

	public String getDymSearchFiedName() {
		return dymSearchFiedName;
	}

	public void setDymSearchFiedName(String dymSearchFiedName) {
		this.dymSearchFiedName = dymSearchFiedName;
	}

	public String getDymSearchFiedVal() {
		return dymSearchFiedVal;
	}

	public void setDymSearchFiedVal(String dymSearchFiedVal) {
		this.dymSearchFiedVal = dymSearchFiedVal;
	}

}