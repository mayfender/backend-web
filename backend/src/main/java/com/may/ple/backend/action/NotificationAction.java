package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.NotificationGetCriteriaReq;
import com.may.ple.backend.criteria.NotificationGetCriteriaResp;
import com.may.ple.backend.service.NotificationService;

@Component
@Path("notification")
public class NotificationAction {
	private static final Logger LOG = Logger.getLogger(NotificationAction.class.getName());
	private NotificationService service;
	
	@Autowired
	public NotificationAction(NotificationService service) {
		this.service = service;
	}
	
	/*@POST
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
	}*/
	
	@POST
	@Path("/get")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp get(NotificationGetCriteriaReq req) {
		LOG.debug("Start");
		NotificationGetCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			resp = service.get(req);
			
		} catch (Exception e) {
			resp = new NotificationGetCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	/*@POST
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
	}*/
	
}