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

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.FileCommonCriteriaResp;
import com.may.ple.backend.criteria.PaymentOnlineChkCriteriaReq;
import com.may.ple.backend.service.PaymentOnlineCheckService;

@Component
@Path("paymentOnlineCheck")
public class PaymentOnlineCheckAction {
	private static final Logger LOG = Logger.getLogger(PaymentOnlineCheckAction.class.getName());
	private PaymentOnlineCheckService service;
	
	@Autowired
	public PaymentOnlineCheckAction(PaymentOnlineCheckService service) {
		this.service = service;
	}
	
	@POST
	@Path("/getCheckListShow")
	@Produces(MediaType.APPLICATION_JSON)
	public FileCommonCriteriaResp getCheckListShow(PaymentOnlineChkCriteriaReq req) {
		LOG.debug("Start");
		FileCommonCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			resp = service.getCheckListShow(req);
		} catch (Exception e) {
			resp = new FileCommonCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/getCheckList")
	@Produces(MediaType.APPLICATION_JSON)
	public FileCommonCriteriaResp getCheckList(PaymentOnlineChkCriteriaReq req) {
		LOG.debug("Start");
		FileCommonCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			resp = service.getCheckList(req);
		} catch (Exception e) {
			resp = new FileCommonCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateChkLst")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateChkLst(PaymentOnlineChkCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateChkLst(req);
		} catch (Exception e) {
			resp = new FileCommonCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getHtml")
	public FileCommonCriteriaResp getHtml(@QueryParam("id")String id, @QueryParam("productId")String productId) {
		LOG.debug("Start");
		FileCommonCriteriaResp resp = new FileCommonCriteriaResp();
		
		try {
			String html = service.getHtml(id, productId);
			resp.setHtml(html);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}
