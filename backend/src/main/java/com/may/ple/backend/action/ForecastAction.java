package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.ForecastFindCriteriaReq;
import com.may.ple.backend.criteria.ForecastFindCriteriaResp;
import com.may.ple.backend.criteria.ForecastResultCriteriaReq;
import com.may.ple.backend.criteria.ForecastResultCriteriaResp;
import com.may.ple.backend.criteria.ForecastSaveCriteriaReq;
import com.may.ple.backend.service.ForecastService;

@Component
@Path("forecast")
public class ForecastAction {
	private static final Logger LOG = Logger.getLogger(ForecastAction.class.getName());
	private ForecastService service;
	
	@Autowired
	public ForecastAction(ForecastService service) {
		this.service = service;
	}
	
	@POST
	@Path("/save")
	public CommonCriteriaResp save(ForecastSaveCriteriaReq req) {
		LOG.debug("Start");
		ForecastFindCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			service.save(req);
			
			ForecastFindCriteriaReq findReq = new ForecastFindCriteriaReq();
			findReq.setProductId(req.getProductId());
			findReq.setContractNo(req.getContractNo());
			findReq.setCurrentPage(req.getCurrentPage());
			findReq.setItemsPerPage(req.getItemsPerPage());
			
			resp = service.find(findReq);
		} catch (Exception e) {
			resp = new ForecastFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp find(ForecastFindCriteriaReq req) {
		LOG.debug("Start");
		ForecastFindCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			resp = service.find(req);
			
		} catch (Exception e) {
			resp = new ForecastFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/remove")
	public CommonCriteriaResp remove(ForecastFindCriteriaReq req) {
		LOG.debug("Start");
		ForecastFindCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			service.remove(req);
			
			ForecastFindCriteriaReq findReq = new ForecastFindCriteriaReq();
			findReq.setProductId(req.getProductId());
			findReq.setContractNo(req.getContractNo());
			findReq.setCurrentPage(req.getCurrentPage());
			findReq.setItemsPerPage(req.getItemsPerPage());
			
			resp = service.find(findReq);
		} catch (Exception e) {
			resp = new ForecastFindCriteriaResp(1000);
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
	
}