package com.may.ple.backend.action;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.may.ple.backend.criteria.LoginDataResp;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.repository.UserRepository;

@RestController
public class LoginAction {
	private static final Logger LOG = Logger.getLogger(UserAction.class.getName());
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginDataResp> user(Principal principal) {
		ResponseEntity<LoginDataResp> responseEntity = null;
		
		try {
			
			Users user = userRepository.findByUserName(principal.getName());
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("userNameShow", user.getUserNameShow());
			
			LoginDataResp loginDataResp = new LoginDataResp(principal, dataMap);
			responseEntity = new ResponseEntity<LoginDataResp>(loginDataResp, HttpStatus.OK);
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
		
        return responseEntity;
    }
	
}
