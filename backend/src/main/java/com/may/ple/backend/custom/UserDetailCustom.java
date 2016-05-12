package com.may.ple.backend.custom;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailCustom implements UserDetails {
	private static final long serialVersionUID = -2010562583792822787L;
	private User user;
	private String showname;
	private String token;

	public UserDetailCustom(final User _user) {
		this.user = _user;
	}

	public UserDetailCustom() {
	}
	
	@Override
	public String toString() {
		return "CustomUserDetails [user=" + user + "]";
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return user.getAuthorities();
	}

	@Override
	public String getPassword() {
		return null;
//		return user.getPassword();
	}

	@Override
	public String getUsername() {
		if (this.user == null) {
			return null;
		}
		return this.user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.user.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.user.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.user.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return this.user.isEnabled();
	}

	public User getUser() {
		return user;
	}
	public String getShowname() {
		return showname;
	}
	public void setShowname(String showname) {
		this.showname = showname;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}

}
