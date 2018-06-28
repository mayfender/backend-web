package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.NotificationCriteriaReq;
import com.may.ple.backend.criteria.NotificationCriteriaResp;
import com.may.ple.backend.service.NotificationService;

@Component
@Path("notification")
public class NotificationAction {
	private static final Logger LOG = Logger.getLogger(NotificationAction.class.getName());
	private NotificationService service;
	
	@Autowired
	public NotificationAction(NotificationService service) {
		this.service = service;
	}
	
	@POST
	@Path("/booking")
	public CommonCriteriaResp booking(NotificationCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.booking(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/getAlert")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp getAlert(NotificationCriteriaReq req) {
		LOG.debug("Start");
		NotificationCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			resp = service.getAlert(req);
			
		} catch (Exception e) {
			resp = new NotificationCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/takeAction")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp takeAction(NotificationCriteriaReq req) {
		LOG.debug("Start");
		NotificationCriteriaResp resp = new NotificationCriteriaResp();
		
		try {
			LOG.debug(req);
			service.takeAction(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/remove")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp remove(NotificationCriteriaReq req) {
		LOG.debug("Start");
		NotificationCriteriaResp resp = new NotificationCriteriaResp();
		
		try {
			LOG.debug(req);
			service.remove(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}