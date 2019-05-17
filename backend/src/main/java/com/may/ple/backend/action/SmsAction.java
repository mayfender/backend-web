package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.ForecastResultCriteriaReq;
import com.may.ple.backend.criteria.ForecastResultCriteriaResp;
import com.may.ple.backend.criteria.ForecastUpdatePaidAmountCriteriaReq;
import com.may.ple.backend.criteria.ForecastUpdatePaidAmountCriteriaResp;
import com.may.ple.backend.criteria.SmsCriteriaReq;
import com.may.ple.backend.criteria.SmsCriteriaResp;
import com.may.ple.backend.service.SmsService;

@Component
@Path("sms")
public class SmsAction {
	private static final Logger LOG = Logger.getLogger(SmsAction.class.getName());
	private SmsService service;
	
	@Autowired
	public SmsAction(SmsService service) {
		this.service = service;
	}
	
	@POST
	@Path("/save")
	public CommonCriteriaResp save(SmsCriteriaReq req) {
		LOG.debug("Start");
		SmsCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			service.save(req);
			
			SmsCriteriaReq findReq = new SmsCriteriaReq();
			findReq.setProductId(req.getProductId());
			findReq.setCurrentPage(req.getCurrentPage());
			findReq.setItemsPerPage(req.getItemsPerPage());
			
			resp = service.get(findReq);
		} catch (Exception e) {
			resp = new SmsCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/get")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp get(SmsCriteriaReq req) {
		LOG.debug("Start");
		SmsCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			resp = service.get(req);
			
		} catch (Exception e) {
			resp = new SmsCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/remove")
	public CommonCriteriaResp remove(SmsCriteriaReq req) {
		LOG.debug("Start");
		SmsCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			service.remove(req);
			
			SmsCriteriaReq findReq = new SmsCriteriaReq();
			findReq.setProductId(req.getProductId());
			findReq.setCurrentPage(req.getCurrentPage());
			findReq.setItemsPerPage(req.getItemsPerPage());
			
			resp = service.get(findReq);
		} catch (Exception e) {
			resp = new SmsCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/forecastResult")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp forecastResult(ForecastResultCriteriaReq req) {
		LOG.debug("Start");
		ForecastResultCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.forecastResult(req, null, false);			
		} catch (Exception e) {
			resp = new ForecastResultCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updatePaidAmount")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updatePaidAmount(ForecastUpdatePaidAmountCriteriaReq req) {
		LOG.debug("Start");
		ForecastUpdatePaidAmountCriteriaResp resp = new ForecastUpdatePaidAmountCriteriaResp() {};
		
		try {
			LOG.debug(req);
			Double paidAmount = service.updatePaidAmount(req);		
			resp.setPaidAmount(paidAmount);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}