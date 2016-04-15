package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.RenewalPrepareDataCriteriaResp;
import com.may.ple.backend.criteria.SptRegisteredFindCriteriaReq;
import com.may.ple.backend.criteria.SptRegisteredFindCriteriaResp;
import com.may.ple.backend.entity.SptMemberType;
import com.may.ple.backend.service.RenewalService;
import com.may.ple.backend.service.SptMemberTypeService;
import com.may.ple.backend.service.SptRegistrationService;

@Component
@Path("renewal")
public class RenewalAction {
	private static final Logger LOG = Logger.getLogger(RenewalAction.class.getName());
	private RenewalService service;
	private SptMemberTypeService sptMemberTypeService;
	private SptRegistrationService registrationService;
	
	@Autowired
	public RenewalAction(RenewalService service, SptMemberTypeService sptMemberTypeService, SptRegistrationService registrationService) {
		this.service = service;
		this.sptMemberTypeService = sptMemberTypeService;
		this.registrationService = registrationService;
	}
	
	@GET
	@Path("/prepareData")
	@Produces(MediaType.APPLICATION_JSON)
	public RenewalPrepareDataCriteriaResp prepareData() {
		LOG.debug("Start");
		RenewalPrepareDataCriteriaResp resp = new RenewalPrepareDataCriteriaResp();
		
		try {
			
			List<SptMemberType> memberTypes = sptMemberTypeService.showMemberType();
			
			resp.setMemberTypes(memberTypes);
			resp.setTodayDate(new LocalDate().toDate());
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/renewal")
	@Produces(MediaType.APPLICATION_JSON)
	public SptRegisteredFindCriteriaResp renewal(SptRegisteredFindCriteriaReq req) {
		LOG.debug("Start");
		SptRegisteredFindCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			service.renewal(req);
			
			resp = registrationService.findRenewal(req);
			registrationService.period(resp.getRegistereds());
			
		} catch (Exception e) {
			resp = new SptRegisteredFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public SptRegisteredFindCriteriaResp updateStatus(SptRegisteredFindCriteriaReq req) {
		LOG.debug("Start");
		SptRegisteredFindCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			service.updateStatus(req.getRegId(), req.getStatus());
			
			resp = registrationService.findRenewal(req);
			registrationService.period(resp.getRegistereds());
			
		} catch (Exception e) {
			resp = new SptRegisteredFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}