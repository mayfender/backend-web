package com.may.ple.backend.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class Provinces implements Serializable {
	private static final long serialVersionUID = -567738520505915915L;
	@Id
	private Long provinceId;
	private String provinceName;
	private String provinceNameEng;
	
	protected Provinces() {}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getProvinceId() {
		return provinceId;
	}
	public void setProvinceId(Long provinceId) {
		this.provinceId = provinceId;
	}
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	public String getProvinceNameEng() {
		return provinceNameEng;
	}
	public void setProvinceNameEng(String provinceNameEng) {
		this.provinceNameEng = provinceNameEng;
	}

}
