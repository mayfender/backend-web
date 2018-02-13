package com.may.ple.backend.action;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;

@Component
@Path("accessManagement")
public class AccessManagementAction {
	private static final Logger LOG = Logger.getLogger(AccessManagementAction.class.getName());
	
/*	@Autowired
	public AccessManagementAction() {

	}*/
	
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
	
	
	@GET
	@Path("/test")
	public CommonCriteriaResp deleteResultCode() {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			LOG.info("Access time is" + String.format("%1$tH%1$tM%1$tS%1$tL", new Date()));
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}