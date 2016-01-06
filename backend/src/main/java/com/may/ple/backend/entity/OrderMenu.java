package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="menu_id", referencedColumnName="id")
	private Menu menu;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDateTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedDateTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date finishedChangedStatusDateTime;
	private Integer status;
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="cus_id", referencedColumnName="id")
	private Customer customer;
	private Integer amount;
	private Integer orderRound;
	private Boolean isTakeHome;
	private Boolean isCancel;
	private String cancelReason;
	private String comment;
	@ManyToMany
	@JoinTable(
			name="order_sub_menu", 
			joinColumns=@JoinColumn(name="order_menu_id", referencedColumnName="id"),
			inverseJoinColumns=@JoinColumn(name="sub_menu_id", referencedColumnName="id")
			)
	private List<SubMenu> subMenus;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
	
	protected OrderMenu(){}
	
	public OrderMenu(Menu menu, Date createdDateTime, Date updatedDateTime, Date finishedChangedStatusDateTime, Integer status, 
			Integer amount, Boolean isTakeHome, Boolean isCancel, Integer orderRound, String comment, Customer customer) {
		this.menu = menu;
		this.createdDateTime = createdDateTime;
		this.updatedDateTime = updatedDateTime;
		this.finishedChangedStatusDateTime = finishedChangedStatusDateTime;
		this.status = status;
		this.amount = amount;
		this.isTakeHome = isTakeHome;
		this.isCancel = isCancel;
		this.orderRound = orderRound;
		this.comment = comment;
		this.customer = customer;
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
	public Date getCreatedDateTime() {
		return createdDateTime;
	}
	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	public void setOrderRound(Integer orderRound) {
		this.orderRound = orderRound;
	}
	public Integer getOrderRound() {
		return orderRound;
	}
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	public Date getFinishedChangedStatusDateTime() {
		return finishedChangedStatusDateTime;
	}
	public void setFinishedChangedStatusDateTime(Date finishedChangedStatusDateTime) {
		this.finishedChangedStatusDateTime = finishedChangedStatusDateTime;
	}

	public List<SubMenu> getSubMenus() {
		return subMenus;
	}

	public void setSubMenus(List<SubMenu> subMenus) {
		this.subMenus = subMenus;
	}
	
}
