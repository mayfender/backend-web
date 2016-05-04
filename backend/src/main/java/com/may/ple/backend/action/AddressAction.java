package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.FindByZipcodeCriteriaResp;
import com.may.ple.backend.entity.Zipcodes;
import com.may.ple.backend.service.ZipcodesService;

@Component
@Path("address")
public class AddressAction {
	private static final Logger LOG = Logger.getLogger(AddressAction.class.getName());
	private ZipcodesService service;
	
	@Autowired
	public AddressAction(ZipcodesService service) {
		this.service = service;
	}
	
	@GET
	@Path("/findByZipcode")
	@Produces(MediaType.APPLICATION_JSON)
	public FindByZipcodeCriteriaResp findByZipcode(@QueryParam("zipcode")String zipcode) {
		LOG.debug("Start");
		FindByZipcodeCriteriaResp resp = new FindByZipcodeCriteriaResp();
		
		try {
			
			LOG.debug("zipcode: " + zipcode);
			List<Zipcodes> zipcodeResult = service.findByZipcode(zipcode);
			resp.setZipcodes(zipcodeResult);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}