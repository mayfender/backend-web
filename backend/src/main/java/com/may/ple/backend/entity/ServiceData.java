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
public class ServiceData implements Serializable {
	private static final long serialVersionUID = -8270223194670407091L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String docNo;
	private String receiver;
	private String sender;
	private String postDest;
	private Double fee;
	private Double otherServicePrice;
	private Double amount;
	private String accName;
	private String bankName;
	private String accNo;
	private String tel;
	private Integer status;
	private Date createdDateTime;
	private Date updatedDateTime;
	private Long serviceTypeId;
	
	protected ServiceData() {}
	
	public ServiceData(String docNo, String receiver, String sender, 
			String postDest, Double amount, Double fee, Double otherServicePrice, 
			String accName, String bankName, String accNo, String tel, Integer status, Long serviceTypeId, 
			Date createdDateTime, Date updatedDateTime) {
		this.docNo = docNo;
		this.receiver = receiver;
		this.sender = sender;
		this.postDest = postDest;
		this.fee = fee;
		this.otherServicePrice = otherServicePrice;
		this.accName = accName;
		this.bankName = bankName;
		this.accNo = accNo;
		this.tel = tel;
		this.status = status;
		this.serviceTypeId = serviceTypeId;
		this.amount = amount;
		this.createdDateTime = createdDateTime;
		this.updatedDateTime = updatedDateTime;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDocNo() {
		return docNo;
	}
	public void setDocNo(String docNo) {
		this.docNo = docNo;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getPostDest() {
		return postDest;
	}
	public void setPostDest(String postDest) {
		this.postDest = postDest;
	}
	public Double getFee() {
		return fee;
	}
	public void setFee(Double fee) {
		this.fee = fee;
	}
	public Double getOtherServicePrice() {
		return otherServicePrice;
	}
	public void setOtherServicePrice(Double otherServicePrice) {
		this.otherServicePrice = otherServicePrice;
	}
	public String getAccName() {
		return accName;
	}
	public void setAccName(String accName) {
		this.accName = accName;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getAccNo() {
		return accNo;
	}
	public void setAccNo(String accNo) {
		this.accNo = accNo;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
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
	public Long getServiceTypeId() {
		return serviceTypeId;
	}
	public void setServiceTypeId(Long serviceTypeId) {
		this.serviceTypeId = serviceTypeId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}

}
