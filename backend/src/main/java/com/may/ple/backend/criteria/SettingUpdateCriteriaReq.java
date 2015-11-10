package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SettingUpdateCriteriaReq {
	private Integer afterPriceRate;
	private Integer beforeHour;
	private Integer beforeHourPriceRate;
	private Integer minuteToHour;
	private Integer pricePerTime;
	private Integer parkingSize;
	private Boolean unlimtedTime;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
	
	public Integer getBeforeHour() {
		return beforeHour;
	}
	public void setBeforeHour(Integer beforeHour) {
		this.beforeHour = beforeHour;
	}
	public Integer getMinuteToHour() {
		return minuteToHour;
	}
	public void setMinuteToHour(Integer minuteToHour) {
		this.minuteToHour = minuteToHour;
	}
	public Integer getPricePerTime() {
		return pricePerTime;
	}
	public void setPricePerTime(Integer pricePerTime) {
		this.pricePerTime = pricePerTime;
	}
	public Boolean getUnlimtedTime() {
		return unlimtedTime;
	}
	public void setUnlimtedTime(Boolean unlimtedTime) {
		this.unlimtedTime = unlimtedTime;
	}

	public Integer getAfterPriceRate() {
		return afterPriceRate;
	}

	public void setAfterPriceRate(Integer afterPriceRate) {
		this.afterPriceRate = afterPriceRate;
	}

	public Integer getBeforeHourPriceRate() {
		return beforeHourPriceRate;
	}

	public void setBeforeHourPriceRate(Integer beforeHourPriceRate) {
		this.beforeHourPriceRate = beforeHourPriceRate;
	}

	public Integer getParkingSize() {
		return parkingSize;
	}

	public void setParkingSize(Integer parkingSize) {
		this.parkingSize = parkingSize;
	}
	
}
