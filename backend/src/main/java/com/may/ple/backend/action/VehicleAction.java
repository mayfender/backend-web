package com.may.ple.backend.action;

import java.sql.Timestamp;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.VehicleCheckOutCriteriaReq;
import com.may.ple.backend.criteria.VehicleCheckOutCriteriaResp;
import com.may.ple.backend.criteria.VehicleSaveCriteriaReq;
import com.may.ple.backend.criteria.VehicleSaveCriteriaResp;
import com.may.ple.backend.criteria.VehicleSearchCriteriaReq;
import com.may.ple.backend.criteria.VehicleSearchCriteriaResp;
import com.may.ple.backend.entity.VehicleParking;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.VehicleService;
import com.may.ple.backend.util.DateTimeUtil;

@Component
@Path("vehicle")
public class VehicleAction {
	private static final Logger LOG = Logger.getLogger(VehicleAction.class.getName());
	private VehicleService service;
	private SimpMessagingTemplate template;
	
	@Autowired
	public VehicleAction(VehicleService service, SimpMessagingTemplate template) {
		this.service = service;
		this.template = template;
	}
	
	@POST
	@Path("/searchVehicleParking")
	@Produces(MediaType.APPLICATION_JSON)
	public VehicleSearchCriteriaResp searchVehicleParking(VehicleSearchCriteriaReq req) {
		VehicleSearchCriteriaResp resp;
		LOG.debug("Start");
		
		try {
			LOG.debug(req);
			
			resp = service.searchVehicleParking(req);
			
			LOG.debug(resp);
		} catch (Exception e) {
			resp = new VehicleSearchCriteriaResp();
			resp.setStatusCode(1000);
			LOG.error(e.toString());
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/checkOutVehicle")
	@Produces(MediaType.APPLICATION_JSON)
	public VehicleCheckOutCriteriaResp checkOutVehicle(VehicleCheckOutCriteriaReq req) {
		VehicleCheckOutCriteriaResp resp = new VehicleCheckOutCriteriaResp();
		LOG.debug("Start");
		
		try {
			LOG.debug(req);
			
			VehicleParking vehicleParking = service.checkOutVehicle(req);
			resp.setVehicleParking(vehicleParking);
			
			try {
				LOG.debug("Call Broker");
				template.convertAndSend("/topic/checkOut", vehicleParking);				
			} catch (Exception e) {
				LOG.error(e.toString());
			}
			
			LOG.debug(resp);
		} catch (CustomerException e) {
			resp.setStatusCode(e.errCode);			
			LOG.error(e.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString());
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/saveVehicleParking")
	@Produces(MediaType.APPLICATION_JSON)
	public VehicleSaveCriteriaResp saveVehicleParking(VehicleSaveCriteriaReq req) {
		VehicleSaveCriteriaResp resp = new VehicleSaveCriteriaResp();
		LOG.debug("Start");
		
		try {
			LOG.debug(req);
			
			Timestamp timestamp = DateTimeUtil.getTimstampNoMillisecond();
			service.saveVehicleParking(req, timestamp);
			
			try {
				LOG.debug("Call Broker");			
				VehicleParking vehicleParking = new VehicleParking(timestamp, null, null, 0, req.getLicenseNo(), null, null);
				template.convertAndSend("/topic/checkIn", vehicleParking);				
			} catch (Exception e) {
				LOG.error(e.toString());
			}
			
			LOG.debug(resp);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString());
		}
		
		LOG.debug("End");
		return resp;
	}
	
}
