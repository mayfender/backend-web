package com.may.ple.backend.model;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public class AuthenticationResponse extends ModelBase {

	private static final long serialVersionUID = -6624726180748515507L;
	private String token;
	private String showname;
	private String username;
	private Collection<? extends GrantedAuthority> authorities;

	public AuthenticationResponse() {
		super();
	}

	public AuthenticationResponse(String token, String showname, String username, Collection<? extends GrantedAuthority> authorities) {
		this.setToken(token);
		this.showname = showname;
		this.username = username;
		this.authorities = authorities;
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

}
