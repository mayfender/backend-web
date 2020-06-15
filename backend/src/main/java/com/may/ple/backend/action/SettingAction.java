package com.may.ple.backend.action;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.SettingCriteriaReq;
import com.may.ple.backend.criteria.SettingCriteriaResp;
import com.may.ple.backend.service.SettingService;

@Component
@Path("setting")
public class SettingAction {
	private static final Logger LOG = Logger.getLogger(SettingAction.class.getName());
	private SettingService service;
	
	@Autowired
	public SettingAction(SettingService service) {
		this.service = service;
	}
	
	@POST
	@Path("/saveUpdateReceiver")
	@Produces(MediaType.APPLICATION_JSON)
	public SettingCriteriaResp saveUpdateReceiver(SettingCriteriaReq req) {
		LOG.debug("Start");
		SettingCriteriaResp resp = new SettingCriteriaResp();
		
		try {
			LOG.debug(req);
			service.saveUpdateReceiver(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getReceiverList")
	public SettingCriteriaResp getReceiverList(@QueryParam("enabled")Boolean enabled) {
		LOG.debug("Start");
		SettingCriteriaResp resp = new SettingCriteriaResp();
		
		try {
			resp.setReceiverList(service.getReceiverList(enabled));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateOrder")
	public SettingCriteriaResp updateOrder(SettingCriteriaReq req) {
		LOG.debug("Start");
		SettingCriteriaResp resp = new SettingCriteriaResp();
		
		try {
			LOG.debug(req);
			service.updateOrder(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}