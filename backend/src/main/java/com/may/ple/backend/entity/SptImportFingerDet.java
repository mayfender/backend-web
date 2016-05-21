package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class SptImportFingerDet implements Serializable {
	private static final long serialVersionUID = -4144871117313499379L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long fingerDetId;
	private String fingerId;
	@Temporal(TemporalType.DATE)
	private Date dateStamp;
	@Temporal(TemporalType.TIME)
	private Date timeStamp;
	private String action;
	@ManyToOne(cascade = {CascadeType.MERGE})
	@JoinColumn(name="finger_file_id")
	private SptImportFingerFile file;
	@Transient
	private String firstname;
	@Transient
	private String lastname;
	
	public SptImportFingerDet() {}
	
	public SptImportFingerDet(String fingerId, Date dateStamp, Date timeStamp, String action, SptImportFingerFile file) {
		this.fingerId = fingerId;
		this.dateStamp = dateStamp;
		this.timeStamp = timeStamp;
		this.action = action;
		this.file = file;
	}
	
	public SptImportFingerDet(String firstname, String lastname, Date dateStamp, Date timeStamp, String action) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.dateStamp = dateStamp;
		this.timeStamp = timeStamp;
		this.action = action;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
	public Long getFingerDetId() {
		return fingerDetId;
	}
	public void setFingerDetId(Long fingerDetId) {
		this.fingerDetId = fingerDetId;
	}
	public String getFingerId() {
		return fingerId;
	}
	public void setFingerId(String fingerId) {
		this.fingerId = fingerId;
	}
	public SptImportFingerFile getFile() {
		return file;
	}
	public void setFile(SptImportFingerFile file) {
		this.file = file;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public Date getDateStamp() {
		return dateStamp;
	}
	public void setDateStamp(Date dateStamp) {
		this.dateStamp = dateStamp;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}

}
