package com.may.ple.backend.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.UserLogCriteriaReq;
import com.may.ple.backend.criteria.UserLogCriteriaResp;
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
	@Path("/getUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public UserLogCriteriaResp getUsers(UserLogCriteriaReq req) {
		LOG.debug("Start");
		UserLogCriteriaResp resp = new UserLogCriteriaResp();

		try {
			List<String> roles = new ArrayList<>();
			roles.add("ROLE_ADMIN");
			roles.add("ROLE_SUPERVISOR");
			roles.add("ROLE_USER");
			resp.setUsers(userService.getUser(req.getProductId(), roles));
		} catch (Exception e) {
			resp = new UserLogCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/getLog")
	@Produces(MediaType.APPLICATION_JSON)
	public UserLogCriteriaResp getLog(UserLogCriteriaReq req) {
		LOG.debug("Start");
		UserLogCriteriaResp resp = new UserLogCriteriaResp();

		try {
			List<Map> logs = service.getLog(req);
			resp.setLogs(logs);
		} catch (Exception e) {
			resp = new UserLogCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		LOG.debug("End");
		return resp;
	}

}