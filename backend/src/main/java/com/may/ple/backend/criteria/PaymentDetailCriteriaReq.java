package com.may.ple.backend.criteria;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PaymentDetailCriteriaReq {
	private String id;
	private String fileId;
	private String productId;
	private Integer currentPage;
	private Integer itemsPerPage;
	private String columnName;
	private String order;
	private String keyword;
	private String contractNo;
	private String owner;
	private Date dateFrom;
	private Date dateTo;
	private Boolean isFillTemplate;
	private Integer pocModule;
	private String kysGroup;
	
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

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
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

	public Boolean getIsFillTemplate() {
		return isFillTemplate;
	}

	public void setIsFillTemplate(Boolean isFillTemplate) {
		this.isFillTemplate = isFillTemplate;
	}

	public Integer getPocModule() {
		return pocModule;
	}

	public void setPocModule(Integer pocModule) {
		this.pocModule = pocModule;
	}

	public String getKysGroup() {
		return kysGroup;
	}

	public void setKysGroup(String kysGroup) {
		this.kysGroup = kysGroup;
	}

}
