package com.may.ple.backend.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.OrderCriteriaReq;
import com.may.ple.backend.criteria.OrderCriteriaResp;
import com.may.ple.backend.service.OrderService;

@Component
@Path("order")
public class OrderAction {
	private static final Logger LOG = Logger.getLogger(OrderAction.class.getName());
	private OrderService service;
	
	@Autowired
	public OrderAction(OrderService service) {
		this.service = service;
	}
	
	@POST
	@Path("/savePeriod")
	@Produces(MediaType.APPLICATION_JSON)
	public OrderCriteriaResp savePeriod(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();
		
		try {
			
			LOG.debug(req);
			service.savePeriod(req);
			
			List<Map> periods = service.getPeriod(req.getUserId());
			resp.setPeriods(periods);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/saveOrder")
	@Produces(MediaType.APPLICATION_JSON)
	public OrderCriteriaResp saveOrder(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();
		
		try {
			LOG.debug(req);
			service.saveOrder(req);
			
			resp.setOrderNameLst(service.getOrderNameByPeriod(req.getUserId(), req.getPeriodId()));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getPeriod")
	public OrderCriteriaResp getPeriod(@QueryParam("userId")String userId) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();
		
		try {
			List<Map> periods = service.getPeriod(userId);
			
			resp.setOrderNameLst(service.getOrderNameByPeriod(userId, periods.get(0).get("_id").toString()));
			resp.setPeriods(periods);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/getSumOrder")
	public OrderCriteriaResp getSumOrder(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();
		
		try {
			List<Integer> types = new ArrayList<>();
			
			if(req.getTab().equals("1")) {
				types.add(1);
				types.add(11);
				types.add(12);
				types.add(13);
				types.add(14);
				types.add(15);
			} else if(req.getTab().equals("2")) {
				types.add(2);
				types.add(21);
			} else if(req.getTab().equals("3")) {
				types.add(3);
				types.add(31);				
			} else if(req.getTab().equals("4")) {
				types.add(4);				
			} else if(req.getTab().equals("5")) {
				types.add(131);				
			}
			
			List<Map> periods = service.getSumOrder(types, req.getOrderName(), req.getPeriodId());
			resp.setOrderData(periods);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}

}
