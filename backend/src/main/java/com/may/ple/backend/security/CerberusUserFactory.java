package com.may.ple.backend.security;

import com.may.ple.backend.entity.Users;

public class CerberusUserFactory {

	public static CerberusUser create(Users user) {
		byte[] photo = null;
		
		if(user.getImgData() != null) {
			photo = user.getImgData().getImgContent();			
		}
		
		return new CerberusUser(
				user.getId(), 
				user.getShowname(),
				user.getUsername(), 
				user.getPassword(), 
				null, 
				null, 
				user.getAuthorities(),
				user.getProducts(),
				user.getSetting(),
				photo
		);
	}

}
