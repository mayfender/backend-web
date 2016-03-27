package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.SptImportFingerDetFindCriteriaReq;
import com.may.ple.backend.criteria.SptImportFingerDetFindCriteriaResp;
import com.may.ple.backend.service.SptImportFingerDetService;

@Component
@Path("fingerDet")
public class SptImportFingerDetAction {
	private static final Logger LOG = Logger.getLogger(SptImportFingerDetAction.class.getName());
	private SptImportFingerDetService service;
	
	@Autowired
	public SptImportFingerDetAction(SptImportFingerDetService service) {
		this.service = service;
	}
	
	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public SptImportFingerDetFindCriteriaResp search(SptImportFingerDetFindCriteriaReq req) {
		LOG.debug("Start");
		SptImportFingerDetFindCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			resp = service.search(req);
			
		} catch (Exception e) {
			resp = new SptImportFingerDetFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
		
}
