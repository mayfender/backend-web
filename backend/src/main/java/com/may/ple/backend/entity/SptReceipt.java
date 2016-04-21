package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class SptReceipt implements Serializable {
	private static final long serialVersionUID = 8902254075756411333L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long receiptId;
	private String receiptNo;
	private Integer receiptType;
	private Date createdDateTime;
	private Date updatedDateTime;
	private Long refId;
	
	protected SptReceipt() {}
	
	public SptReceipt(String receiptNo, Integer receiptType, Date createdDateTime, Date updatedDateTime, Long refId) {
		this.receiptNo = receiptNo;
		this.receiptType = receiptType;
		this.createdDateTime = createdDateTime;
		this.updatedDateTime = updatedDateTime;
		this.refId = refId;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getReceiptId() {
		return receiptId;
	}
	public void setReceiptId(Long receiptId) {
		this.receiptId = receiptId;
	}
	public String getReceiptNo() {
		return receiptNo;
	}
	public void setReceiptNo(String receiptNo) {
		this.receiptNo = receiptNo;
	}
	public Integer getReceiptType() {
		return receiptType;
	}
	public void setReceiptType(Integer receiptType) {
		this.receiptType = receiptType;
	}
	public Date getCreatedDateTime() {
		return createdDateTime;
	}
	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	public Date getUpdatedDateTime() {
		return updatedDateTime;
	}
	public void setUpdatedDateTime(Date updatedDateTime) {
		this.updatedDateTime = updatedDateTime;
	}
	public Long getRefId() {
		return refId;
	}
	public void setRefId(Long refId) {
		this.refId = refId;
	}
	
}
