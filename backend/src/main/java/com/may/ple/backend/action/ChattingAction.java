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

import com.may.ple.backend.criteria.ChattingCriteriaReq;
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
	public CommonCriteriaResp getFriends(@QueryParam("currentPage")Integer currentPage, @QueryParam("itemsPerPage")Integer itemsPerPage, @QueryParam("keyword")String keyword) {
		LOG.debug("Start");
		ChattingCriteriaResp resp = new ChattingCriteriaResp();
		
		try {
			resp.setFriends(service.getFriends(currentPage, itemsPerPage, keyword));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/getChat")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp getChat(ChattingCriteriaReq req) {
		LOG.debug("Start");
		ChattingCriteriaResp resp = null;
		
		try {
			LOG.debug(req);			
			service.getChat();
		} catch (Exception e) {
			resp = new ChattingCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
		
}