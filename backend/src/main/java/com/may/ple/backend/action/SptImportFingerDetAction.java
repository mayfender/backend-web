package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
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
	public CommonCriteriaResp search() {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug("");
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
		
}
