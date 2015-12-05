package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class OrderMenu implements Serializable {
	private static final long serialVersionUID = -9127347553101093822L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	private Menu menu;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedDateTime;
	private Integer status;
	private Long cusId;
	private Integer amount;
	private Boolean isTakeHome;
	private Boolean isCancel;
	private String cancelReason;
	private String comment;
	@ManyToOne
	@JoinColumn(name="order_round_id", referencedColumnName="id")
	private OrderRound orderRound;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
	
	protected OrderMenu(){}
	
	public OrderMenu(Menu menu, Date updatedDateTime, Integer status, OrderRound orderRound) {
		this.menu = menu;
		this.updatedDateTime = updatedDateTime;
		this.status = status;
		this.orderRound = orderRound;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Menu getMenu() {
		return menu;
	}
	public void setMenu(Menu menu) {
		this.menu = menu;
	}
	public Date getUpdatedDateTime() {
		return updatedDateTime;
	}
	public void setUpdatedDateTime(Date updatedDateTime) {
		this.updatedDateTime = updatedDateTime;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Long getCusId() {
		return cusId;
	}
	public void setCusId(Long cusId) {
		this.cusId = cusId;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public OrderRound getOrderRound() {
		return orderRound;
	}
	public void setOrderRound(OrderRound orderRound) {
		this.orderRound = orderRound;
	}
	public Boolean getIsTakeHome() {
		return isTakeHome;
	}
	public void setIsTakeHome(Boolean isTakeHome) {
		this.isTakeHome = isTakeHome;
	}
	public Boolean getIsCancel() {
		return isCancel;
	}
	public void setIsCancel(Boolean isCancel) {
		this.isCancel = isCancel;
	}
	public String getCancelReason() {
		return cancelReason;
	}
	public void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}
	
}
