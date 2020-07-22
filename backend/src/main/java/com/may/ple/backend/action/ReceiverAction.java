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

import com.may.ple.backend.criteria.ReceiverCriteriaReq;
import com.may.ple.backend.criteria.ReceiverCriteriaResp;
import com.may.ple.backend.service.ReceiverService;

@Component
@Path("receiver")
public class ReceiverAction {
	private static final Logger LOG = Logger.getLogger(ReceiverAction.class.getName());
	private ReceiverService service;

	@Autowired
	public ReceiverAction(ReceiverService service) {
		this.service = service;
	}

	@POST
	@Path("/saveUpdateReceiver")
	@Produces(MediaType.APPLICATION_JSON)
	public ReceiverCriteriaResp saveUpdateReceiver(ReceiverCriteriaReq req) {
		LOG.debug("Start");
		ReceiverCriteriaResp resp = new ReceiverCriteriaResp();

		try {
			LOG.debug(req);
			service.saveUpdateReceiver(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@GET
	@Path("/getReceiverList")
	public ReceiverCriteriaResp getReceiverList(@QueryParam("enabled")Boolean enabled) {
		LOG.debug("Start");
		ReceiverCriteriaResp resp = new ReceiverCriteriaResp();

		try {
			resp.setReceiverList(service.getReceiverList(enabled));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/updateOrder")
	public ReceiverCriteriaResp updateOrder(ReceiverCriteriaReq req) {
		LOG.debug("Start");
		ReceiverCriteriaResp resp = new ReceiverCriteriaResp();

		try {
			LOG.debug(req);
			service.updateOrder(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

}