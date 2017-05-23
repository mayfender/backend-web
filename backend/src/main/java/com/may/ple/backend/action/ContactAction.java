package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.SentMailCriteriaReq;
import com.may.ple.backend.service.ContactService;

@Component
@Path("contact")
public class ContactAction {
	private static final Logger LOG = Logger.getLogger(ContactAction.class.getName());
	private ContactService service;
	
	@Autowired
	public ContactAction(ContactService service) {
		this.service = service;
	}
	
	@POST
	@Path("/sentMail")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp sentMail(SentMailCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug(req);
			service.sentMail(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}