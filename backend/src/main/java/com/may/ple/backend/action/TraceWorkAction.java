package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.TraceFindCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaResp;
import com.may.ple.backend.criteria.TraceSaveCriteriaReq;
import com.may.ple.backend.criteria.TraceSaveCriteriaResp;
import com.may.ple.backend.service.TraceWorkService;

@Component
@Path("traceWork")
public class TraceWorkAction {
	private static final Logger LOG = Logger.getLogger(TraceWorkAction.class.getName());
	private TraceWorkService service;
	
	@Autowired
	public TraceWorkAction(TraceWorkService service) {
		this.service = service;
	}
	
	@POST
	@Path("/find")
	public TraceFindCriteriaResp find(TraceFindCriteriaReq req) {
		LOG.debug("Start");
		TraceFindCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			resp = service.find(req);
			
		} catch (Exception e) {
			resp = new TraceFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/save")
	public TraceSaveCriteriaResp save(TraceSaveCriteriaReq req) {
		LOG.debug("Start");
		TraceSaveCriteriaResp resp = new TraceSaveCriteriaResp();
		
		try {
			
			LOG.debug(req);
			service.save(req);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}