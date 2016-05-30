package com.may.ple.backend.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;

import com.may.ple.backend.entity.UserSetting;

public class AuthenticationResponse extends ModelBase {

	private static final long serialVersionUID = -6624726180748515507L;
	private String token;
	private String showname;
	private String username;
	private Collection<? extends GrantedAuthority> authorities;
	private List<Map<String, String>> products;
	private UserSetting setting;

	public AuthenticationResponse() {
		super();
	}

	public AuthenticationResponse(String token, String showname, String username, Collection<? extends GrantedAuthority> authorities, 
								  List<Map<String, String>> products, UserSetting setting) {
		this.setToken(token);
		this.showname = showname;
		this.username = username;
		this.authorities = authorities;
		this.products = products;
		this.setting = setting;
	}

	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getShowname() {
		return showname;
	}

	public void setShowname(String showname) {
		this.showname = showname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	public List<Map<String, String>> getProducts() {
		return products;
	}

	public void setProducts(List<Map<String, String>> products) {
		this.products = products;
	}

	public UserSetting getSetting() {
		return setting;
	}

	public void setSetting(UserSetting setting) {
		this.setting = setting;
	}

}
