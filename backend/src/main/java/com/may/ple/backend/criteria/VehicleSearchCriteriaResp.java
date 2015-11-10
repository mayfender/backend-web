package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.VehicleParking;

public class VehicleSearchCriteriaResp extends CommonCriteriaResp {
	private List<VehicleParking> vehicleParkings;
	private Long totalItems;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public List<VehicleParking> getVehicleParkings() {
		return vehicleParkings;
	}

	public void setVehicleParkings(List<VehicleParking> vehicleParkings) {
		this.vehicleParkings = vehicleParkings;
	}

	public Long getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Long totalItems) {
		this.totalItems = totalItems;
	}
	
}
