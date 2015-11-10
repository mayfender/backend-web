package com.may.ple.backend.action;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.may.ple.backend.criteria.GreetingResp;

@RestController
public class GreetingAction {
	private static final Logger LOG = Logger.getLogger(GreetingAction.class.getName());
	
	@RequestMapping("/hello")
	public GreetingResp hello(@RequestParam String param) {
		LOG.debug("Hi " + param);
		GreetingResp resp = new GreetingResp();
		
		try {			
			resp.setResult("Hi");
			resp.setDescription("Testing");
		} catch (Exception e) {
			LOG.error(e.toString());
		}
		
		return resp;
	}

}
