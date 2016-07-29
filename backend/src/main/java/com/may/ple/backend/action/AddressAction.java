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

import com.may.ple.backend.criteria.AddressFindCriteriaReq;
import com.may.ple.backend.criteria.AddressFindCriteriaResp;
import com.may.ple.backend.criteria.AddressSaveCriteriaReq;
import com.may.ple.backend.criteria.AddressSaveCriteriaResp;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.entity.Address;
import com.may.ple.backend.service.AddressService;

@Component
@Path("address")
public class AddressAction {
	private static final Logger LOG = Logger.getLogger(AddressAction.class.getName());
	private AddressService service;
	
	@Autowired
	public AddressAction(AddressService service) {
		this.service = service;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public AddressFindCriteriaResp find(AddressFindCriteriaReq req) {
		LOG.debug("Start");
		AddressFindCriteriaResp resp = new AddressFindCriteriaResp();
		
		try {
			
			LOG.debug(req);
			List<Address> addresses = service.find(req);
			resp.setAddresses(addresses);
			
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
	public AddressSaveCriteriaResp save(AddressSaveCriteriaReq req) {
		LOG.debug("Start");
		AddressSaveCriteriaResp resp = new AddressSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String id = service.save(req);
			
			resp.setId(id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/delete")
	public CommonCriteriaResp delete(@QueryParam("id")String id, @QueryParam("productId")String productId) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			service.delete(id, productId);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}