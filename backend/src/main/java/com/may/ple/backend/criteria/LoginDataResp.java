package com.may.ple.backend.criteria;

import java.security.Principal;
import java.util.Map;


public class LoginDataResp {
	private Principal principal;
	private Map<String, Object> dataMap;
	
	public LoginDataResp(Principal principal, Map<String, Object> dataMap) {
		this.principal = principal;
		this.dataMap = dataMap;
	}

	public Principal getPrincipal() {
		return principal;
	}

	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}

	public Map<String, Object> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, Object> dataMap) {
		this.dataMap = dataMap;
	}

}
