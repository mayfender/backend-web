package com.may.ple.backend.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class Amphures implements Serializable {
	private static final long serialVersionUID = -567738520505915915L;
	@Id
	private Long amphurId;
	private String amphurName;
	private String amphurNameEng;
	
	protected Amphures() {}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getAmphurId() {
		return amphurId;
	}
	public void setAmphurId(Long amphurId) {
		this.amphurId = amphurId;
	}
	public String getAmphurName() {
		return amphurName;
	}
	public void setAmphurName(String amphurName) {
		this.amphurName = amphurName;
	}
	public String getAmphurNameEng() {
		return amphurNameEng;
	}
	public void setAmphurNameEng(String amphurNameEng) {
		this.amphurNameEng = amphurNameEng;
	}

}
