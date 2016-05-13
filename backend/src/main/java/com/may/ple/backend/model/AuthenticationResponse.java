package com.may.ple.backend.model;

public class AuthenticationResponse extends ModelBase {

	private static final long serialVersionUID = -6624726180748515507L;
	private String token;
	private String showname;

	public AuthenticationResponse() {
		super();
	}

	public AuthenticationResponse(String token, String showname) {
		this.setToken(token);
		this.showname = showname;
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

}
