package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.ReportMenuCriteriaResp;
import com.may.ple.backend.criteria.ReportMoneyCriteriaReq;
import com.may.ple.backend.criteria.ReportMoneyCriteriaResp;
import com.may.ple.backend.service.ReportService;

@Component
@Path("report")
public class ReportAction {
	private static final Logger LOG = Logger.getLogger(ReportAction.class.getName());
	private ReportService service;
	
	@Autowired
	public ReportAction(ReportService service) {
		this.service = service;
	}
	
	@POST
	@Path("/money")
	@Produces(MediaType.APPLICATION_JSON)
	public ReportMoneyCriteriaResp money(ReportMoneyCriteriaReq req) {
		LOG.debug("Start");
		ReportMoneyCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			resp = service.money(req);
		} catch (Exception e) {
			resp = new ReportMoneyCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/menu")
	@Produces(MediaType.APPLICATION_JSON)
	public ReportMenuCriteriaResp menu(ReportMoneyCriteriaReq req) {
		LOG.debug("Start");
		ReportMenuCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			resp = service.menu(req);
		} catch (Exception e) {
			resp = new ReportMenuCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}
