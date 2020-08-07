package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PriceListCriteriaReq {
	private String id;
	private String priceListName;
	private Double priceBon3;
	private Double priceBon2;
	private Double priceLang2;
	private Double priceTod;
	private Double priceLoy;
	private Double pricePare4;
	private Double pricePare5;
	private Double priceRunBon;
	private Double priceRunLang;
	private Double percentBon3;
	private Double percentBon2;
	private Double percentLang2;
	private Double percentTod;
	private Double percentLoy;
	private Double percentPare4;
	private Double percentPare5;
	private Double percentRunBon;
	private Double percentRunLang;
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

	public String getPriceListName() {
		return priceListName;
	}

	public void setPriceListName(String priceListName) {
		this.priceListName = priceListName;
	}

	public Double getPriceBon3() {
		return priceBon3;
	}

	public void setPriceBon3(Double priceBon3) {
		this.priceBon3 = priceBon3;
	}

	public Double getPriceBon2() {
		return priceBon2;
	}

	public void setPriceBon2(Double priceBon2) {
		this.priceBon2 = priceBon2;
	}

	public Double getPriceLang2() {
		return priceLang2;
	}

	public void setPriceLang2(Double priceLang2) {
		this.priceLang2 = priceLang2;
	}

	public Double getPriceTod() {
		return priceTod;
	}

	public void setPriceTod(Double priceTod) {
		this.priceTod = priceTod;
	}

	public Double getPriceLoy() {
		return priceLoy;
	}

	public void setPriceLoy(Double priceLoy) {
		this.priceLoy = priceLoy;
	}

	public Double getPricePare4() {
		return pricePare4;
	}

	public void setPricePare4(Double pricePare4) {
		this.pricePare4 = pricePare4;
	}

	public Double getPricePare5() {
		return pricePare5;
	}

	public void setPricePare5(Double pricePare5) {
		this.pricePare5 = pricePare5;
	}

	public Double getPriceRunBon() {
		return priceRunBon;
	}

	public void setPriceRunBon(Double priceRunBon) {
		this.priceRunBon = priceRunBon;
	}

	public Double getPriceRunLang() {
		return priceRunLang;
	}

	public void setPriceRunLang(Double priceRunLang) {
		this.priceRunLang = priceRunLang;
	}

	public Double getPercentBon3() {
		return percentBon3;
	}

	public void setPercentBon3(Double percentBon3) {
		this.percentBon3 = percentBon3;
	}

	public Double getPercentBon2() {
		return percentBon2;
	}

	public void setPercentBon2(Double percentBon2) {
		this.percentBon2 = percentBon2;
	}

	public Double getPercentLang2() {
		return percentLang2;
	}

	public void setPercentLang2(Double percentLang2) {
		this.percentLang2 = percentLang2;
	}

	public Double getPercentTod() {
		return percentTod;
	}

	public void setPercentTod(Double percentTod) {
		this.percentTod = percentTod;
	}

	public Double getPercentLoy() {
		return percentLoy;
	}

	public void setPercentLoy(Double percentLoy) {
		this.percentLoy = percentLoy;
	}

	public Double getPercentPare4() {
		return percentPare4;
	}

	public void setPercentPare4(Double percentPare4) {
		this.percentPare4 = percentPare4;
	}

	public Double getPercentPare5() {
		return percentPare5;
	}

	public void setPercentPare5(Double percentPare5) {
		this.percentPare5 = percentPare5;
	}

	public Double getPercentRunBon() {
		return percentRunBon;
	}

	public void setPercentRunBon(Double percentRunBon) {
		this.percentRunBon = percentRunBon;
	}

	public Double getPercentRunLang() {
		return percentRunLang;
	}

	public void setPercentRunLang(Double percentRunLang) {
		this.percentRunLang = percentRunLang;
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
