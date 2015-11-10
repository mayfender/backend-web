package com.may.ple.backend.action;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.ReportCriteriaResp;
import com.may.ple.backend.service.ReportService;

@Component
@Path("report")
public class ReportAction {
	private static final Logger LOG = Logger.getLogger(ReportAction.class.getName());
	private ReportService reportService;
	
	@Autowired
	public ReportAction(ReportService reportService) {
		this.reportService = reportService;
	}
	
	@GET
	@Path("/reportVehicleYear")
	@Secured("ROLE_ADMIN")
	public ReportCriteriaResp reportVehicleYear() {
		ReportCriteriaResp resp = new ReportCriteriaResp();
		LOG.debug("Start");
		
		try {
			
			Map<String, List<String>> yearResult = reportService.reportVehicleYear();
			List<String> years = yearResult.get("years");
			
			if(years != null && years.size() > 0) {
				ReportCriteriaResp reportMonth = reportVehicleMonth(years.get(0));
				Map<String, List<String>> monthResult = reportMonth.getResult();
				List<String> monthValues = monthResult.get("values");
				
				if(monthValues != null && monthValues.size() > 0) {
					yearResult.put("monthValues", monthValues);
				}
			}
			
			resp.setResult(yearResult);
			
			LOG.debug(resp);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString());
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/reportMoneyYear")
	@Secured("ROLE_ADMIN")
	public ReportCriteriaResp reportMoneyYear() {
		ReportCriteriaResp resp = new ReportCriteriaResp();
		LOG.debug("Start");
		
		try {
			
			Map<String, List<String>> yearResult = reportService.reportMoneyYear();
			List<String> years = yearResult.get("years");
			
			if(years != null && years.size() > 0) {
				ReportCriteriaResp reportMonth = reportMoneyMonth(years.get(0));
				Map<String, List<String>> monthResult = reportMonth.getResult();
				List<String> monthValues = monthResult.get("values");
				
				if(monthValues != null && monthValues.size() > 0) {
					yearResult.put("monthValues", monthValues);
				}
			}
			
			resp.setResult(yearResult);
			
			LOG.debug(resp);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString());
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/reportVehicleMonth")
	@Secured("ROLE_ADMIN")
	public ReportCriteriaResp reportVehicleMonth(@QueryParam("year") String year) {
		ReportCriteriaResp resp = new ReportCriteriaResp();
		LOG.debug("Start");
		
		try {
			LOG.debug("year: " + year);
			
			resp.setResult(reportService.reportVehicleMonth(year));
			
			LOG.debug(resp);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString());
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/reportMoneyMonth")
	@Secured("ROLE_ADMIN")
	public ReportCriteriaResp reportMoneyMonth(@QueryParam("year") String year) {
		ReportCriteriaResp resp = new ReportCriteriaResp();
		LOG.debug("Start");
		
		try {
			LOG.debug("year: " + year);
			
			resp.setResult(reportService.reportMoneyMonth(year));
			
			LOG.debug(resp);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString());
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/reportVehicleDay")
	@Secured("ROLE_ADMIN")
	public ReportCriteriaResp reportVehicleDay(@QueryParam("year") String year, @QueryParam("month") String month) {
		ReportCriteriaResp resp = new ReportCriteriaResp();
		LOG.debug("Start");
		
		try {
			LOG.debug("year: " + year + ", month: " + month);
			resp.setResult(reportService.reportVehicleDay(year, month));
			
			LOG.debug(resp);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString());
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/reportMoneyDay")
	@Secured("ROLE_ADMIN")
	public ReportCriteriaResp reportMoneyDay(@QueryParam("year") String year, @QueryParam("month") String month) {
		ReportCriteriaResp resp = new ReportCriteriaResp();
		LOG.debug("Start");
		
		try {
			LOG.debug("year: " + year + ", month: " + month);
			resp.setResult(reportService.reportMoneyDay(year, month));
			
			LOG.debug(resp);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString());
		}
		
		LOG.debug("End");
		return resp;
	}

}
