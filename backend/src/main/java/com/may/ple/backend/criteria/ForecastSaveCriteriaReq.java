package com.may.ple.backend.criteria;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ForecastSaveCriteriaReq {
	private String id;
	private Integer payTypeId;
	private Integer round;
	private Integer totalRound;
	private Date appointDate;
	private Double appointAmount;
	private Integer forecastPercentage;
	private Double paidAmount;
	private String comment;
	private String productId;
	private String contractNo;
	
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

	public Integer getPayTypeId() {
		return payTypeId;
	}

	public void setPayTypeId(Integer payTypeId) {
		this.payTypeId = payTypeId;
	}

	public Integer getRound() {
		return round;
	}

	public void setRound(Integer round) {
		this.round = round;
	}

	public Integer getTotalRound() {
		return totalRound;
	}

	public void setTotalRound(Integer totalRound) {
		this.totalRound = totalRound;
	}

	public Date getAppointDate() {
		return appointDate;
	}

	public void setAppointDate(Date appointDate) {
		this.appointDate = appointDate;
	}

	public Double getAppointAmount() {
		return appointAmount;
	}

	public void setAppointAmount(Double appointAmount) {
		this.appointAmount = appointAmount;
	}

	public Integer getForecastPercentage() {
		return forecastPercentage;
	}

	public void setForecastPercentage(Integer forecastPercentage) {
		this.forecastPercentage = forecastPercentage;
	}

	public Double getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(Double paidAmount) {
		this.paidAmount = paidAmount;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

}
