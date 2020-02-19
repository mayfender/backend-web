package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.LpsCriteriaReq;
import com.may.ple.backend.criteria.LpsCriteriaResp;
import com.may.ple.backend.service.LpsService;

@Component
@Path("lps")
public class LpsAction {
	private static final Logger LOG = Logger.getLogger(LpsAction.class.getName());
	private LpsService service;
	
	@Autowired
	public LpsAction(LpsService service) {
		this.service = service;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public LpsCriteriaResp find(LpsCriteriaReq req) {
		LOG.debug("Start");
		LpsCriteriaResp resp = new LpsCriteriaResp();
		
		try {
			LOG.debug(req);
			resp = service.find(req);
		} catch (Exception e) {
			resp = new LpsCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}