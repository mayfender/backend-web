package com.may.ple.backend.action;

import java.util.List;

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
import com.may.ple.backend.criteria.MasterNamingDetailCriteriaReq;
import com.may.ple.backend.criteria.MasterNamingDetailCriteriaResp;
import com.may.ple.backend.criteria.MasterNamingDetailSaveCriteriaResp;
import com.may.ple.backend.entity.SptMasterNamingDet;
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
			List<SptMasterNamingDet> namingDetails = detailService.findMasterDetail(req);
			resp.setNamingDetails(namingDetails);
			
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
	public MasterNamingDetailSaveCriteriaResp save(MasterNamingDetailCriteriaReq req) {
		LOG.debug("Start");
		MasterNamingDetailSaveCriteriaResp resp = new MasterNamingDetailSaveCriteriaResp() {};
		
		try {
			
			LOG.debug(req);
			
			if(req.getMasterNamingDetailId() == null) {
				Long id = detailService.save(req);		
				resp.setNamingDetId(id);
			} else {
				detailService.update(req);
				resp.setNamingDetId(req.getMasterNamingDetailId());
			}
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
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
	}

}
