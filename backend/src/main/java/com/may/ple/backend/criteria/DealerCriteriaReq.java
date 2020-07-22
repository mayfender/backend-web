package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.Dealer;

public class DealerCriteriaReq {
	/*private String id;
	private String name;
	private Boolean enabled;
	private String dbname;
	private String ip;
	private Integer port;
	private String username;
	private String password;*/
	private Dealer dealer;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Dealer getDealer() {
		return dealer;
	}

	public void setDealer(Dealer dealer) {
		this.dealer = dealer;
	}

}
