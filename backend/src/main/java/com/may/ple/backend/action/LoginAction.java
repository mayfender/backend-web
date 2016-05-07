package com.may.ple.backend.action;

import java.security.Principal;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.may.ple.backend.criteria.LoginCriteriaResp;
import com.mongodb.DB;

@RestController
public class LoginAction {
	private static final Logger LOG = Logger.getLogger(LoginAction.class.getName());
	@Autowired
	private MongoDbFactory mongo;
//	@Autowired
//	private UserRepository userRepository;
	
	@RequestMapping("/user")
	public LoginCriteriaResp user(Principal user) {
		LoginCriteriaResp resp = new LoginCriteriaResp();
		
		try {			
//			Users u = userRepository.findByUserName(user.getName());
			
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("userNameShow", u.getUserNameShow());
			
//			resp.setMap(map);
//			resp.setPrincipal(user);
			
			DB db = mongo.getDb();
			LOG.debug(db);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
		return resp;
	}

}
