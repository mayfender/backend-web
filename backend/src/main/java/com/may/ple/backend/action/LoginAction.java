package com.may.ple.backend.action;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.may.ple.backend.criteria.LoginCriteriaResp;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.repository.UserRepository;

@RestController
public class LoginAction {
	private static final Logger LOG = Logger.getLogger(UserAction.class.getName());
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/user")
	public LoginCriteriaResp user(Principal user) {
		LoginCriteriaResp resp = new LoginCriteriaResp();
		
		try {			
			Users u = userRepository.findByUserName(user.getName());
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userNameShow", u.getUserNameShow());
			
			resp.setMap(map);
			resp.setPrincipal(user);
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
		return resp;
	}

}
