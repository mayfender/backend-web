package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.OrderSearchCriteriaResp;
import com.may.ple.backend.criteria.OrderUpdateCriteriaReq;
import com.may.ple.backend.entity.OrderMenu;
import com.may.ple.backend.service.OderService;

@Component
@Path("order")
public class OrderAction {
	private static final Logger LOG = Logger.getLogger(OrderAction.class.getName());
	private OderService oderService;
	
	@Autowired
	public OrderAction(OderService oderService) {
		this.oderService = oderService;
	}
		
	@GET
	@Path("/findOrderByCus")
	public OrderSearchCriteriaResp findOrderByCus(@QueryParam("cusId") Long cusId) {
		LOG.debug("Start");
		OrderSearchCriteriaResp resp = new OrderSearchCriteriaResp();
		
		try {			
			LOG.debug(cusId);
			
			List<OrderMenu> tables = oderService.findOrderByCus(cusId);
			resp.setOrders(tables);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/deleteByOrderId")
	public CommonCriteriaResp cancelByOrderId(@QueryParam("id") Long id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {			
			LOG.debug(id);
			
			oderService.cancelByOrderId(id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateOrder")
	public CommonCriteriaResp updateOrder(OrderUpdateCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug(req);
			oderService.updateOrder(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}
