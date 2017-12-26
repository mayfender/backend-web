package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.PaymentDetailCriteriaReq;
import com.may.ple.backend.criteria.PaymentDetailCriteriaResp;
import com.may.ple.backend.service.PaymentDetailService;

@Component
@Path("paymentDetail")
public class PaymentDetailAction {
	private static final Logger LOG = Logger.getLogger(PaymentDetailAction.class.getName());
	private PaymentDetailService service;
	
	@Autowired
	public PaymentDetailAction(PaymentDetailService service) {
		this.service = service;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public PaymentDetailCriteriaResp find(PaymentDetailCriteriaReq req) {
		LOG.debug("Start");
		PaymentDetailCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.find(req, false, null, null);
		} catch (Exception e) {
			resp = new PaymentDetailCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}
