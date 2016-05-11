package com.may.ple.backend.action;

import java.security.Principal;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.may.ple.backend.criteria.LoginCriteriaResp;

@RestController
public class LoginAction {
	private static final Logger LOG = Logger.getLogger(LoginAction.class.getName());
	
	@RequestMapping("/user")
	public LoginCriteriaResp user(Principal user) {
		LoginCriteriaResp resp = new LoginCriteriaResp();
		
		try {			
			
			resp.setPrincipal(user);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
		return resp;
	}

}
