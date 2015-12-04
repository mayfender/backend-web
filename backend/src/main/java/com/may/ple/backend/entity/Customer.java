package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class Customer implements Serializable {
	private static final long serialVersionUID = -4550041761355871749L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String ref;
	private String tableDetail;
	private Integer status;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDateTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedDateTime;
	
	protected Customer(){}
	
	public Customer(String ref, String tableDetail, Integer status, Date createdDateTime, Date updatedDateTime){
		this.ref = ref;
		this.tableDetail = tableDetail;
		this.status = status;
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
	public String getRef() {
		return ref;
	}
	public void setRef(String ref) {
		this.ref = ref;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
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
	public String getTableDetail() {
		return tableDetail;
	}
	public void setTableDetail(String tableDetail) {
		this.tableDetail = tableDetail;
	}
	
}
