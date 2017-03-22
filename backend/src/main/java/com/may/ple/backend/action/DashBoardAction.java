package com.may.ple.backend.action;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.DashBoardCriteriaReq;
import com.may.ple.backend.criteria.DashboardTraceCountCriteriaResp;
import com.may.ple.backend.service.DashBoardService;

@Component
@Path("dashBoard")
public class DashBoardAction {
	private static final Logger LOG = Logger.getLogger(DashBoardAction.class.getName());
	private DashBoardService service;
	
	@Autowired
	public DashBoardAction(DashBoardService service) {
		this.service = service;
	}
	
	@POST
	@Path("/traceCount")
	@Produces(MediaType.APPLICATION_JSON)
	public DashboardTraceCountCriteriaResp traceCount(DashBoardCriteriaReq req) {
		LOG.debug("Start");
		DashboardTraceCountCriteriaResp resp = new DashboardTraceCountCriteriaResp();
		
		try {
			
			LOG.debug(req);
			
			List<Integer> statuses = new ArrayList<>();
			statuses.add(0);
			statuses.add(1);
			
			req.setStatuses(statuses);
			
			service.traceCount(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
		
}