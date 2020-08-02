package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.PersistUserCriteriaReq;
import com.may.ple.backend.criteria.ProfileGetCriteriaResp;
import com.may.ple.backend.criteria.ProfileUpdateCriteriaReq;
import com.may.ple.backend.criteria.ProfileUpdateCriteriaResp;
import com.may.ple.backend.criteria.UserEditCriteriaReq;
import com.may.ple.backend.criteria.UserEditCriteriaResp;
import com.may.ple.backend.criteria.UserSearchCriteriaReq;
import com.may.ple.backend.criteria.UserSearchCriteriaResp;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.UserService;

@Component
@Path("user")
public class UserAction {
	private static final Logger LOG = Logger.getLogger(UserAction.class.getName());
	private UserService service;

	@Autowired
	public UserAction(UserService service) {
		this.service = service;
	}

	@POST
	@Path("/findUserAll")
	@Produces(MediaType.APPLICATION_JSON)
	public UserSearchCriteriaResp findUserAll(UserSearchCriteriaReq req) {
		LOG.debug("Start");
		UserSearchCriteriaResp resp = null;

		try {
			LOG.debug(req);
			resp = service.findAllUser(req);
		} catch (Exception e) {
			resp = new UserSearchCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/getUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public UserSearchCriteriaResp getUsers(UserSearchCriteriaReq req) {
		LOG.debug("Start");
		UserSearchCriteriaResp resp = new UserSearchCriteriaResp();

		try {
			List<Users> users = service.getUsers(req);
			resp.setUsers(users);
		} catch (Exception e) {
			resp = new UserSearchCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/saveUser")
	public CommonCriteriaResp saveUser(PersistUserCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};

		try {
			LOG.debug(req);
			service.saveUser(req);
		} catch (CustomerException cx) {
			resp.setStatusCode(cx.errCode);
			LOG.error(cx.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/editUser")
	public UserEditCriteriaResp editUser(UserEditCriteriaReq req) {
		LOG.debug("Start");
		UserEditCriteriaResp resp = new UserEditCriteriaResp();

		try {
			LOG.debug(req);
			LOG.debug("Get user data.");
			Users user = service.editUser(req.getUserId());
			resp.setUser(user);
		} catch (CustomerException cx) {
			resp.setStatusCode(cx.errCode);
			LOG.error(cx.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/updateUser")
	public CommonCriteriaResp updateUser(PersistUserCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};

		try {
			LOG.debug(req);
			service.updateUser(req);
		} catch (CustomerException cx) {
			resp.setStatusCode(cx.errCode);
			LOG.error(cx.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/deleteUser")
	public UserSearchCriteriaResp deleteUser(UserSearchCriteriaReq req) {
		LOG.debug("Start");
		UserSearchCriteriaResp resp;

		try {
			LOG.debug(req);
			service.deleteUser(req.getUserId());

			resp = findUserAll(req);
		} catch (Exception e) {
			resp = new UserSearchCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@GET
	@Path("/getProfile")
	public ProfileGetCriteriaResp getProfile(@QueryParam("username")String username) {
		LOG.debug("Start");
		ProfileGetCriteriaResp resp = new ProfileGetCriteriaResp();

		try {
			LOG.debug(username);
			Users user = service.getProfile(username);
			resp.setUser(user);
		} catch (CustomerException cx) {
			resp.setStatusCode(cx.errCode);
			LOG.error(cx.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/updateProfile")
	public ProfileUpdateCriteriaResp updateProfile(ProfileUpdateCriteriaReq req) {
		LOG.debug("Start");
		ProfileUpdateCriteriaResp resp = null;

		try {
			LOG.debug(req);
			resp = service.updateProfile(req);
		} catch (CustomerException cx) {
			resp = new ProfileUpdateCriteriaResp(cx.errCode);
			LOG.error(cx.toString());
		} catch (Exception e) {
			resp = new ProfileUpdateCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

}
