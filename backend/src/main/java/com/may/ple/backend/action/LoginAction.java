package com.may.ple.backend.action;

import java.security.Principal;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginAction {
	
	@RequestMapping("/user")
	public Principal user(Principal user) {
		return user;
	}

}
