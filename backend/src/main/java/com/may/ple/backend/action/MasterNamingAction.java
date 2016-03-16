package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.MasterNamingDetailCriteriaReq;
import com.may.ple.backend.criteria.MasterNamingDetailCriteriaResp;
import com.may.ple.backend.entity.MasterNamingDetail;
import com.may.ple.backend.service.MasterNamingDetailService;

@Component
@Path("masterNaming")
public class MasterNamingAction {
	private static final Logger LOG = Logger.getLogger(MasterNamingAction.class.getName());
	private MasterNamingDetailService detailService;
	
	@Autowired
	public MasterNamingAction(MasterNamingDetailService detailService) {
		this.detailService = detailService;
	}
	
	@POST
	@Path("/findDetail")
	@Produces(MediaType.APPLICATION_JSON)
	public MasterNamingDetailCriteriaResp findDetail(MasterNamingDetailCriteriaReq req) {
		LOG.debug("Start");
		MasterNamingDetailCriteriaResp resp = new MasterNamingDetailCriteriaResp();
		
		try {
			
			LOG.debug(req);
			List<MasterNamingDetail> namingDetails = detailService.findByMasterId(req.getMasterNamingId());
			resp.setNamingDetails(namingDetails);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

}
