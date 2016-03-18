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

import com.may.ple.backend.criteria.SptRegisteredFindCriteriaReq;
import com.may.ple.backend.criteria.SptRegisteredFindCriteriaResp;
import com.may.ple.backend.criteria.SptRegistrationEditCriteriaResp;
import com.may.ple.backend.criteria.SptRegistrationSaveCriteriaReq;
import com.may.ple.backend.criteria.SptRegistrationSaveCriteriaResp;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.SptRegistrationService;

@Component
@Path("registration")
public class SptRegistrationAction {
	private static final Logger LOG = Logger.getLogger(SptRegistrationAction.class.getName());
	private SptRegistrationService service;
	
	
	@Autowired
	public SptRegistrationAction(SptRegistrationService service) {
		this.service = service;
	}
	
	@POST
	@Path("/findRegistered")
	@Produces(MediaType.APPLICATION_JSON)
	public SptRegisteredFindCriteriaResp findRegistered(SptRegisteredFindCriteriaReq req) {
		LOG.debug("Start");
		SptRegisteredFindCriteriaResp resp = new SptRegisteredFindCriteriaResp();
		
		try {
			
			LOG.debug(req);
			resp = service.findRegistered(req);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/saveRegistration")
	@Produces(MediaType.APPLICATION_JSON)
	public SptRegistrationSaveCriteriaResp saveRegistration(SptRegistrationSaveCriteriaReq req) {
		LOG.debug("Start");
		SptRegistrationSaveCriteriaResp resp = new SptRegistrationSaveCriteriaResp();
		
		try {
			
			LOG.debug(req);
			service.saveRegistration(req);
			
		} catch (CustomerException e) {			
			resp.setStatusCode(e.errCode);
			LOG.error(e.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/editRegistration")
	@Produces(MediaType.APPLICATION_JSON)
	public SptRegistrationEditCriteriaResp editRegistration(@QueryParam("id")Long id) {
		LOG.debug("Start");
		SptRegistrationEditCriteriaResp resp = new SptRegistrationEditCriteriaResp();
		
		try {
			
			LOG.debug("id: " + id);
			service.editRegistration(id);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}
