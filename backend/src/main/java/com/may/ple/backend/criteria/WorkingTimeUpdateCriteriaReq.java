package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class WorkingTimeUpdateCriteriaReq {
	private String productId;
	private Integer normalStartTimeH;
	private Integer normalStartTimeM;
	private Integer normalEndTimeH;
	private Integer normalEndTimeM;
	private Integer satStartTimeH;
	private Integer satStartTimeM;
	private Integer satEndTimeH;
	private Integer satEndTimeM;
	private Integer sunStartTimeH;
	private Integer sunStartTimeM;
	private Integer sunEndTimeH;
	private Integer sunEndTimeM;
	private Boolean normalWorkingDayEnable;
	private Boolean satWorkingDayEnable;
	private Boolean sunWorkingDayEnable;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public Integer getNormalStartTimeH() {
		return normalStartTimeH;
	}

	public void setNormalStartTimeH(Integer normalStartTimeH) {
		this.normalStartTimeH = normalStartTimeH;
	}

	public Integer getNormalStartTimeM() {
		return normalStartTimeM;
	}

	public void setNormalStartTimeM(Integer normalStartTimeM) {
		this.normalStartTimeM = normalStartTimeM;
	}

	public Integer getNormalEndTimeH() {
		return normalEndTimeH;
	}

	public void setNormalEndTimeH(Integer normalEndTimeH) {
		this.normalEndTimeH = normalEndTimeH;
	}

	public Integer getNormalEndTimeM() {
		return normalEndTimeM;
	}

	public void setNormalEndTimeM(Integer normalEndTimeM) {
		this.normalEndTimeM = normalEndTimeM;
	}

	public Integer getSatStartTimeH() {
		return satStartTimeH;
	}

	public void setSatStartTimeH(Integer satStartTimeH) {
		this.satStartTimeH = satStartTimeH;
	}

	public Integer getSatStartTimeM() {
		return satStartTimeM;
	}

	public void setSatStartTimeM(Integer satStartTimeM) {
		this.satStartTimeM = satStartTimeM;
	}

	public Integer getSatEndTimeH() {
		return satEndTimeH;
	}

	public void setSatEndTimeH(Integer satEndTimeH) {
		this.satEndTimeH = satEndTimeH;
	}

	public Integer getSatEndTimeM() {
		return satEndTimeM;
	}

	public void setSatEndTimeM(Integer satEndTimeM) {
		this.satEndTimeM = satEndTimeM;
	}

	public Integer getSunStartTimeH() {
		return sunStartTimeH;
	}

	public void setSunStartTimeH(Integer sunStartTimeH) {
		this.sunStartTimeH = sunStartTimeH;
	}

	public Integer getSunStartTimeM() {
		return sunStartTimeM;
	}

	public void setSunStartTimeM(Integer sunStartTimeM) {
		this.sunStartTimeM = sunStartTimeM;
	}

	public Integer getSunEndTimeH() {
		return sunEndTimeH;
	}

	public void setSunEndTimeH(Integer sunEndTimeH) {
		this.sunEndTimeH = sunEndTimeH;
	}

	public Integer getSunEndTimeM() {
		return sunEndTimeM;
	}

	public void setSunEndTimeM(Integer sunEndTimeM) {
		this.sunEndTimeM = sunEndTimeM;
	}

	public Boolean getNormalWorkingDayEnable() {
		return normalWorkingDayEnable;
	}

	public void setNormalWorkingDayEnable(Boolean normalWorkingDayEnable) {
		this.normalWorkingDayEnable = normalWorkingDayEnable;
	}

	public Boolean getSatWorkingDayEnable() {
		return satWorkingDayEnable;
	}

	public void setSatWorkingDayEnable(Boolean satWorkingDayEnable) {
		this.satWorkingDayEnable = satWorkingDayEnable;
	}

	public Boolean getSunWorkingDayEnable() {
		return sunWorkingDayEnable;
	}

	public void setSunWorkingDayEnable(Boolean sunWorkingDayEnable) {
		this.sunWorkingDayEnable = sunWorkingDayEnable;
	}

}
