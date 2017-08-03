package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.ForecastFindCriteriaReq;
import com.may.ple.backend.criteria.ForecastFindCriteriaResp;
import com.may.ple.backend.criteria.ForecastSaveCriteriaReq;
import com.may.ple.backend.criteria.ForecastSaveCriteriaResp;
import com.may.ple.backend.service.ForecastService;

@Component
@Path("forecast")
public class ForecastAction {
	private static final Logger LOG = Logger.getLogger(ForecastAction.class.getName());
	private ForecastService service;
	
	@Autowired
	public ForecastAction(ForecastService service) {
		this.service = service;
	}
	
	@POST
	@Path("/save")
	public CommonCriteriaResp save(ForecastSaveCriteriaReq req) {
		LOG.debug("Start");
		ForecastSaveCriteriaResp resp = new ForecastSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String id = service.save(req);
			
			resp.setId(id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp find(ForecastFindCriteriaReq req) {
		LOG.debug("Start");
		ForecastFindCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			resp = service.find(req);
			
		} catch (Exception e) {
			resp = new ForecastFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}