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

import com.may.ple.backend.criteria.SendRoundCriteriaReq;
import com.may.ple.backend.criteria.SendRoundCriteriaResp;
import com.may.ple.backend.service.SendRoundService;

@Component
@Path("sendRound")
public class SendRoundAction {
	private static final Logger LOG = Logger.getLogger(SendRoundAction.class.getName());
	private SendRoundService service;

	@Autowired
	public SendRoundAction(SendRoundService service) {
		this.service = service;
	}

	@POST
	@Path("/saveUpdate")
	@Produces(MediaType.APPLICATION_JSON)
	public SendRoundCriteriaResp saveUpdate(SendRoundCriteriaReq req) {
		LOG.debug("Start");
		SendRoundCriteriaResp resp = new SendRoundCriteriaResp();

		try {
			LOG.debug(req);
			service.saveUpdate(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@GET
	@Path("/getDataList")
	public SendRoundCriteriaResp getDataList(@QueryParam("enabled")Boolean enabled, @QueryParam("dealerId")String dealerId) {
		LOG.debug("Start");
		SendRoundCriteriaResp resp = new SendRoundCriteriaResp();

		try {
			resp.setDataList(service.getDataList(enabled, dealerId));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/updateOrder")
	public SendRoundCriteriaResp updateOrder(SendRoundCriteriaReq req) {
		LOG.debug("Start");
		SendRoundCriteriaResp resp = new SendRoundCriteriaResp();

		try {
			service.updateOrder(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/statusToggle")
	public SendRoundCriteriaResp statusToggle(SendRoundCriteriaReq req) {
		LOG.debug("Start");
		SendRoundCriteriaResp resp = new SendRoundCriteriaResp();

		try {
			service.statusToggle(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

}