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

import com.may.ple.backend.criteria.BankAccCriteriaResp;
import com.may.ple.backend.criteria.BankAccSaveCriteriaReq;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.CustomerComSaveCriteriaReq;
import com.may.ple.backend.criteria.ListSaveCriteriaResp;
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
	
	@GET
	@Path("/findAccNo")
	@Produces(MediaType.APPLICATION_JSON)
	public BankAccCriteriaResp findAccNo() {
		LOG.debug("Start");
		BankAccCriteriaResp resp = null;
		
		try {			
			resp = service.findAccNo();
		} catch (Exception e) {
			resp = new BankAccCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/saveAccNo")
	@Produces(MediaType.APPLICATION_JSON)
	public ListSaveCriteriaResp saveAccNo(BankAccSaveCriteriaReq req) {
		LOG.debug("Start");
		ListSaveCriteriaResp resp = new ListSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String id = service.saveAccNo(req);
			
			resp.setId(id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateCusCompany")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateCusCompany(CustomerComSaveCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug(req);
			service.updateCusCompany(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateCusCompanyEmail")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateCusCompanyEmail(CustomerComSaveCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug(req);
			service.updateCusCompanyEmail(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/deleteAccNo")
	public CommonCriteriaResp deleteAccNo(@QueryParam("id")String id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			service.deleteAccNo(id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}