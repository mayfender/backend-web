package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ReceiverCriteriaReq {
	private String id;
	private String receiverName;
	private String senderName;
	private Double passPriceBon3;
	private Double passPriceBon2;
	private Double passPriceLang2;
	private Double passPriceTod;
	private Double passPriceLoy1;
	private Double passPriceLoy4;
	private Double passPriceLoy5;
	private Double passPerBon3;
	private Double passPerBon2;
	private Double passPerLang2;
	private Double passPerTod;
	private Double passPerLoy1;
	private Double passPerLoy4;
	private Double passPerLoy5;
	private Double salePriceBon3;
	private Double salePriceBon2;
	private Double salePriceLang2;
	private Double salePriceTod;
	private Double salePriceLoy1;
	private Double salePriceLoy4;
	private Double salePriceLoy5;
	private Double salePerBon3;
	private Double salePerBon2;
	private Double salePerLang2;
	private Double salePerTod;
	private Double salePerLoy1;
	private Double salePerLoy4;
	private Double salePerLoy5;
	private List<Map> orderData;
	private String dealerId;
	private Boolean enabled;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public Double getPassPriceBon3() {
		return passPriceBon3;
	}

	public void setPassPriceBon3(Double passPriceBon3) {
		this.passPriceBon3 = passPriceBon3;
	}

	public Double getPassPriceBon2() {
		return passPriceBon2;
	}

	public void setPassPriceBon2(Double passPriceBon2) {
		this.passPriceBon2 = passPriceBon2;
	}

	public Double getPassPriceLang2() {
		return passPriceLang2;
	}

	public void setPassPriceLang2(Double passPriceLang2) {
		this.passPriceLang2 = passPriceLang2;
	}

	public Double getPassPriceTod() {
		return passPriceTod;
	}

	public void setPassPriceTod(Double passPriceTod) {
		this.passPriceTod = passPriceTod;
	}

	public Double getPassPriceLoy1() {
		return passPriceLoy1;
	}

	public void setPassPriceLoy1(Double passPriceLoy1) {
		this.passPriceLoy1 = passPriceLoy1;
	}

	public Double getPassPriceLoy4() {
		return passPriceLoy4;
	}

	public void setPassPriceLoy4(Double passPriceLoy4) {
		this.passPriceLoy4 = passPriceLoy4;
	}

	public Double getPassPriceLoy5() {
		return passPriceLoy5;
	}

	public void setPassPriceLoy5(Double passPriceLoy5) {
		this.passPriceLoy5 = passPriceLoy5;
	}

	public Double getPassPerBon3() {
		return passPerBon3;
	}

	public void setPassPerBon3(Double passPerBon3) {
		this.passPerBon3 = passPerBon3;
	}

	public Double getPassPerBon2() {
		return passPerBon2;
	}

	public void setPassPerBon2(Double passPerBon2) {
		this.passPerBon2 = passPerBon2;
	}

	public Double getPassPerLang2() {
		return passPerLang2;
	}

	public void setPassPerLang2(Double passPerLang2) {
		this.passPerLang2 = passPerLang2;
	}

	public Double getPassPerTod() {
		return passPerTod;
	}

	public void setPassPerTod(Double passPerTod) {
		this.passPerTod = passPerTod;
	}

	public Double getPassPerLoy1() {
		return passPerLoy1;
	}

	public void setPassPerLoy1(Double passPerLoy1) {
		this.passPerLoy1 = passPerLoy1;
	}

	public Double getPassPerLoy4() {
		return passPerLoy4;
	}

	public void setPassPerLoy4(Double passPerLoy4) {
		this.passPerLoy4 = passPerLoy4;
	}

	public Double getPassPerLoy5() {
		return passPerLoy5;
	}

	public void setPassPerLoy5(Double passPerLoy5) {
		this.passPerLoy5 = passPerLoy5;
	}

	public Double getSalePriceBon3() {
		return salePriceBon3;
	}

	public void setSalePriceBon3(Double salePriceBon3) {
		this.salePriceBon3 = salePriceBon3;
	}

	public Double getSalePriceBon2() {
		return salePriceBon2;
	}

	public void setSalePriceBon2(Double salePriceBon2) {
		this.salePriceBon2 = salePriceBon2;
	}

	public Double getSalePriceLang2() {
		return salePriceLang2;
	}

	public void setSalePriceLang2(Double salePriceLang2) {
		this.salePriceLang2 = salePriceLang2;
	}

	public Double getSalePriceTod() {
		return salePriceTod;
	}

	public void setSalePriceTod(Double salePriceTod) {
		this.salePriceTod = salePriceTod;
	}

	public Double getSalePriceLoy1() {
		return salePriceLoy1;
	}

	public void setSalePriceLoy1(Double salePriceLoy1) {
		this.salePriceLoy1 = salePriceLoy1;
	}

	public Double getSalePriceLoy4() {
		return salePriceLoy4;
	}

	public void setSalePriceLoy4(Double salePriceLoy4) {
		this.salePriceLoy4 = salePriceLoy4;
	}

	public Double getSalePriceLoy5() {
		return salePriceLoy5;
	}

	public void setSalePriceLoy5(Double salePriceLoy5) {
		this.salePriceLoy5 = salePriceLoy5;
	}

	public Double getSalePerBon3() {
		return salePerBon3;
	}

	public void setSalePerBon3(Double salePerBon3) {
		this.salePerBon3 = salePerBon3;
	}

	public Double getSalePerBon2() {
		return salePerBon2;
	}

	public void setSalePerBon2(Double salePerBon2) {
		this.salePerBon2 = salePerBon2;
	}

	public Double getSalePerLang2() {
		return salePerLang2;
	}

	public void setSalePerLang2(Double salePerLang2) {
		this.salePerLang2 = salePerLang2;
	}

	public Double getSalePerTod() {
		return salePerTod;
	}

	public void setSalePerTod(Double salePerTod) {
		this.salePerTod = salePerTod;
	}

	public Double getSalePerLoy1() {
		return salePerLoy1;
	}

	public void setSalePerLoy1(Double salePerLoy1) {
		this.salePerLoy1 = salePerLoy1;
	}

	public Double getSalePerLoy4() {
		return salePerLoy4;
	}

	public void setSalePerLoy4(Double salePerLoy4) {
		this.salePerLoy4 = salePerLoy4;
	}

	public Double getSalePerLoy5() {
		return salePerLoy5;
	}

	public void setSalePerLoy5(Double salePerLoy5) {
		this.salePerLoy5 = salePerLoy5;
	}

	public List<Map> getOrderData() {
		return orderData;
	}

	public void setOrderData(List<Map> orderData) {
		this.orderData = orderData;
	}

	public String getDealerId() {
		return dealerId;
	}

	public void setDealerId(String dealerId) {
		this.dealerId = dealerId;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

}
