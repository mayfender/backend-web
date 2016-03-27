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

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.ServiceDataEditCriteriaResp;
import com.may.ple.backend.criteria.ServiceDataFindCriteriaReq;
import com.may.ple.backend.criteria.ServiceDataFindCriteriaResp;
import com.may.ple.backend.criteria.ServiceDataSaveCriteriaReq;
import com.may.ple.backend.exception.CustomerException;
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
	public ServiceDataFindCriteriaResp findServiceData(ServiceDataFindCriteriaReq req) {
		LOG.debug("Start");
		ServiceDataFindCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			resp = service.findServiceData(req);			
			
		} catch (Exception e) {
			resp = new ServiceDataFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/save")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp save(ServiceDataSaveCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			LOG.debug(req);
			service.save(req);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/edit")
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceDataEditCriteriaResp edit(@QueryParam("id") Long id) {
		LOG.debug("Start");
		ServiceDataEditCriteriaResp resp = new ServiceDataEditCriteriaResp() {};
		
		try {
			
			LOG.debug("ID: " + id);
			ServiceDataSaveCriteriaReq serviceData = service.edit(id);
			resp.setServiceData(serviceData);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp update(ServiceDataSaveCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			LOG.debug(req);
			service.update(req);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/print")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp print(@QueryParam("id") Long id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			LOG.debug("ID: " + id);
			service.print(id);
			
		} catch (CustomerException e) {			
			resp.setStatusCode(e.errCode);
			LOG.error(e.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceDataFindCriteriaResp delete(ServiceDataFindCriteriaReq req) {
		LOG.debug("Start");
		ServiceDataFindCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			
			service.delete(req.getId());	
			resp = findServiceData(req);
		} catch (Exception e) {
			resp = new ServiceDataFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}
