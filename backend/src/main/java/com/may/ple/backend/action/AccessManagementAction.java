package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.StampCriteriaReq;
import com.may.ple.backend.service.AccessManagementService;

@Component
@Path("accessManagement")
public class AccessManagementAction {
	private static final Logger LOG = Logger.getLogger(AccessManagementAction.class.getName());
	private AccessManagementService service;
	
	@Autowired
	public AccessManagementAction(AccessManagementService service) {
		this.service = service;
	}
	
	/*@POST
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public ActionCodeFindCriteriaResp findActionCode(ActionCodeFindCriteriaReq req) {
		LOG.debug("Start");
		ActionCodeFindCriteriaResp resp = new ActionCodeFindCriteriaResp();
		
		try {
			
			LOG.debug(req);
			
			List<Integer> statuses = new ArrayList<>();
			statuses.add(0);
			statuses.add(1);
			
			req.setStatuses(statuses);
			
			List<ActionCode> actionCodes = service.findActionCode(req);
			resp.setActionCodes(actionCodes);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}*/
	
	
	@POST
	@Path("/saveRestTimeOut")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp saveRestTimeOut(StampCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			LOG.debug(req);
			service.saveRestTimeOut(req);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}