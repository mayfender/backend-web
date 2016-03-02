package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.ServiceDataFindCriteriaReq;
import com.may.ple.backend.criteria.ServiceDataFindCriteriaResp;
import com.may.ple.backend.service.ServiceDataService;

@Component
@Path("serviceData")
public class ServiceDataAction {
	private static final Logger LOG = Logger.getLogger(ServiceDataAction.class.getName());
	private ServiceDataService service;
	
	@Autowired
	public ServiceDataAction(ServiceDataService service) {
		this.service = service;
	}
	
	@POST
	@Path("/findServiceData")
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceDataFindCriteriaResp findUserAll(ServiceDataFindCriteriaReq req) {
		LOG.debug("Start");
		ServiceDataFindCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			
//			Page<ServiceData> serviceDataPage = service.findServiceData(req);
//			
//			resp.setServiceDatas(serviceDataPage.getContent());
//			resp.setTotalItems(serviceDataPage.getTotalElements());
			
			resp = service.findServiceData(req);
			
			
		} catch (Exception e) {
			resp = new ServiceDataFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}
