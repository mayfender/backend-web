package com.may.ple.backend.action;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.UserLogCriteriaReq;
import com.may.ple.backend.criteria.UserLogCriteriaResp;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.service.UserLogService;
import com.may.ple.backend.service.UserService;

@Component
@Path("userLog")
public class UserLogAction {
	private static final Logger LOG = Logger.getLogger(UserLogAction.class.getName());
	private UserLogService service;
	private UserService userService;

	@Autowired
	public UserLogAction(UserLogService service, UserService userService) {
		this.userService = userService;
		this.service = service;
	}

	@POST
	@Path("/getLog")
	@Produces(MediaType.APPLICATION_JSON)
	public UserLogCriteriaResp getLog(UserLogCriteriaReq req) {
		LOG.debug("Start");
		UserLogCriteriaResp resp;

		try {
			//---:
			resp = service.getLog(req);

			//---:
			if(req.getInit() != null && req.getInit()) {
				List<String> roles = new ArrayList<>();
				roles.add("ROLE_MANAGER");
				List<Users> users1 = userService.getUser(null, roles);

				roles = new ArrayList<>();
				roles.add("ROLE_ADMIN");
				roles.add("ROLE_SUPERVISOR");
				roles.add("ROLE_USER");
				List<Users> users2 = userService.getUser(req.getProductId(), roles);

				if(users1 != null && users1.size() > 0) {
					users2.addAll(users1);
				}

				resp.setUsers(users2);
			}
		} catch (Exception e) {
			resp = new UserLogCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		LOG.debug("End");
		return resp;
	}

}