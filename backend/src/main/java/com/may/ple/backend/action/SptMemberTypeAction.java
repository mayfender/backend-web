package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.SptMemberTypeFindCriteriaReq;
import com.may.ple.backend.criteria.SptMemberTypeFindCriteriaResp;
import com.may.ple.backend.criteria.SptMemberTypeSaveCriteriaReq;
import com.may.ple.backend.entity.SptMemberType;
import com.may.ple.backend.service.SptMemberTypeService;

@Component
@Path("memberType")
public class SptMemberTypeAction {
	private static final Logger LOG = Logger.getLogger(SptMemberTypeAction.class.getName());
	private SptMemberTypeService service;
	
	@Autowired
	public SptMemberTypeAction(SptMemberTypeService service) {
		this.service = service;
	}
	
	@POST
	@Path("/findMemberType")
	@Produces(MediaType.APPLICATION_JSON)
	public SptMemberTypeFindCriteriaResp findMemberType(SptMemberTypeFindCriteriaReq req) {
		LOG.debug("Start");
		SptMemberTypeFindCriteriaResp resp = new SptMemberTypeFindCriteriaResp();
		
		try {
			
			LOG.debug(req);
			List<SptMemberType> memberTypes = service.findMemberType(req);
			resp.setMemberTyps(memberTypes);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/save")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp save(SptMemberTypeSaveCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			LOG.debug(req);
			service.saveMemberType(req);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	
	
	
	
	
	
	
	
	
	/*
	@GET
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp delete(@QueryParam("id") Long id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			LOG.debug("id: " + id);
			detailService.delete(id);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}*/

}
