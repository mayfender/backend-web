package com.may.ple.backend.criteria;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.OrderName;

public class OrderCriteriaResp extends CommonCriteriaResp {
	private List<Map> periods;
	private List<Map> orderData;
	private List orderNameLst;
	private OrderName orderName;
	private Double totalPriceSum;
	private Double totalPriceSumAll;
	private List<Map> result3;
	private List<Map> resultBon2;
	private List<Map> resultLang2;
	private List<Map> resultTod;
	
	public OrderCriteriaResp() {}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
	
	public OrderCriteriaResp(int statusCode) {
		super(statusCode);
	}

	public List<Map> getPeriods() {
		return periods;
	}

	public void setPeriods(List<Map> periods) {
		this.periods = periods;
	}

	public List<Map> getOrderData() {
		return orderData;
	}

	public void setOrderData(List<Map> orderData) {
		this.orderData = orderData;
	}

	public OrderName getOrderName() {
		return orderName;
	}

	public void setOrderName(OrderName orderName) {
		this.orderName = orderName;
	}

	public List getOrderNameLst() {
		return orderNameLst;
	}

	public void setOrderNameLst(List orderNameLst) {
		this.orderNameLst = orderNameLst;
	}

	public Double getTotalPriceSum() {
		return totalPriceSum;
	}

	public void setTotalPriceSum(Double totalPriceSum) {
		this.totalPriceSum = totalPriceSum;
	}

	public Double getTotalPriceSumAll() {
		return totalPriceSumAll;
	}

	public void setTotalPriceSumAll(Double totalPriceSumAll) {
		this.totalPriceSumAll = totalPriceSumAll;
	}

	public List<Map> getResult3() {
		return result3;
	}

	public void setResult3(List<Map> result3) {
		this.result3 = result3;
	}

	public List<Map> getResultBon2() {
		return resultBon2;
	}

	public void setResultBon2(List<Map> resultBon2) {
		this.resultBon2 = resultBon2;
	}

	public List<Map> getResultLang2() {
		return resultLang2;
	}

	public void setResultLang2(List<Map> resultLang2) {
		this.resultLang2 = resultLang2;
	}

	public List<Map> getResultTod() {
		return resultTod;
	}

	public void setResultTod(List<Map> resultTod) {
		this.resultTod = resultTod;
	}

}
