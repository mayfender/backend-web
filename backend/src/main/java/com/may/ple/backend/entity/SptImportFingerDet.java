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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class SptImportFingerDet implements Serializable {
	private static final long serialVersionUID = -4144871117313499379L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long fingerDetId;
	private String fingerId;
	private Date dateTime;
	private String inOut;
	@ManyToOne(cascade = {CascadeType.MERGE})
	@JoinColumn(name="finger_file_id")
	private SptImportFingerFile file;	
	
	public SptImportFingerDet() {}
	
	public SptImportFingerDet(String fingerId, Date dateTime, String inOut, SptImportFingerFile file) {
		this.fingerId = fingerId;
		this.dateTime = dateTime;
		this.inOut = inOut;
		this.file = file;
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
	public Date getDateTime() {
		return dateTime;
	}
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	public SptImportFingerFile getFile() {
		return file;
	}
	public void setFile(SptImportFingerFile file) {
		this.file = file;
	}
	public String getInOut() {
		return inOut;
	}
	public void setInOut(String inOut) {
		this.inOut = inOut;
	}

}
