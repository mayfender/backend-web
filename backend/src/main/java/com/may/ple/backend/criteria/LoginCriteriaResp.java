package com.may.ple.backend.criteria;

import java.security.Principal;
import java.util.Map;

public class LoginCriteriaResp {
	private Principal principal;
	private Map<String, Object> map;

	public Principal getPrincipal() {
		return principal;
	}
	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}
	public Map<String, Object> getMap() {
		return map;
	}
	public void setMap(Map<String, Object> map) {
		this.map = map;
	}

}
