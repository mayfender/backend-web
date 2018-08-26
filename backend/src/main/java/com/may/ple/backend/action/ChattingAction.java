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
	
	@GET
	@Path("/getLastChatFriend")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp getLastChatFriend(@QueryParam("productId")String productId) {
		LOG.debug("Start");
		ChattingCriteriaResp resp = new ChattingCriteriaResp();
		
		try {
			resp.setMapData(service.getLastChatFriend(productId));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getChatMsg")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp getChatMsg(@QueryParam("chattingId")String chattingId, @QueryParam("friendId")String friendId, @QueryParam("isGroup")Boolean isGroup) {
		LOG.debug("Start");
		ChattingCriteriaResp resp;
		
		try {
			resp = service.getChatMsg(chattingId, friendId, isGroup);
		} catch (Exception e) {
			resp = new ChattingCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/sendMsg")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp sendMsg(ChattingCriteriaReq req) {
		LOG.debug("Start");
		ChattingCriteriaResp resp = null;
		
		try {
			resp = service.sendMsg(req);
		} catch (Exception e) {
			resp = new ChattingCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getThumbnail")
	public CommonCriteriaResp getThumbnail(@QueryParam("userId")String userId) {
		LOG.debug("Start");
		ChattingCriteriaResp resp = new ChattingCriteriaResp();
		
		try {
			String thumbnail = service.getThumbnail(userId);
			resp.setThumbnail(thumbnail);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/read")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp read(ChattingCriteriaReq req) {
		LOG.debug("Start");
		ChattingCriteriaResp resp = new ChattingCriteriaResp(){};
		
		try {
			service.read(req.getChattingId(), req.getFriendId());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
		
}