package com.may.ple.backend.security;

import com.may.ple.backend.entity.Users;

public class CerberusUserFactory {

	public static CerberusUser create(Users user) {
		return new CerberusUser(
				user.getId(), 
				user.getShowname(),
				user.getUsername(), 
				user.getPassword(), 
				null, 
				null, 
				user.getAuthorities()
		);
	}

}
