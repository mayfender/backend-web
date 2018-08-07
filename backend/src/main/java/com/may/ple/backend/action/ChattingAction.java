package com.may.ple.backend.action;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.ChattingCriteriaResp;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.service.ChattingService;

@Component
@Path("chatting")
public class ChattingAction {
	private static final Logger LOG = Logger.getLogger(ChattingAction.class.getName());
	private ChattingService service;
	
	@Autowired
	public ChattingAction(ChattingService service) {
		this.service = service;
	}
	
	@GET
	@Path("/getFriends")
	public CommonCriteriaResp getFriends(@QueryParam("currentPage")Integer currentPage, @QueryParam("itemsPerPage")Integer itemsPerPage) {
		LOG.debug("Start");
		ChattingCriteriaResp resp = new ChattingCriteriaResp();
		
		try {
			resp.setFriends(service.getFriends(currentPage, itemsPerPage));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	/*@POST
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
	}*/
		
}