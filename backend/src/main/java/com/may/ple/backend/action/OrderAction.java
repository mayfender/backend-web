package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.OrderSaveCriteriaReq;
import com.may.ple.backend.criteria.OrderSearchCriteriaResp;
import com.may.ple.backend.criteria.OrderUpdateCriteriaReq;
import com.may.ple.backend.entity.Customer;
import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.OrderMenu;
import com.may.ple.backend.service.OderService;

@Component
@Path("order")
public class OrderAction {
	private static final Logger LOG = Logger.getLogger(OrderAction.class.getName());
	private OderService oderService;
	private SimpMessagingTemplate template;
	
	@Autowired
	public OrderAction(OderService oderService, SimpMessagingTemplate template) {
		this.oderService = oderService;
		this.template = template;
	}
		
	@GET
	@Path("/findOrderByCus")
	public OrderSearchCriteriaResp findOrderByCus(@QueryParam("cusId") Long cusId) {
		LOG.debug("Start");
		OrderSearchCriteriaResp resp = null;
		
		try {			
			LOG.debug(cusId);
			
			resp = oderService.findOrderByCus(cusId);
		} catch (Exception e) {
			resp = new OrderSearchCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/searchOrder")
	public OrderSearchCriteriaResp searchOrder() {
		LOG.debug("Start");
		OrderSearchCriteriaResp resp = null;
		
		try {			
			resp = oderService.searchOrder();
		} catch (Exception e) {
			resp = new OrderSearchCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/saveOrder")
	public CommonCriteriaResp saveOrder(OrderSaveCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug(req);
			
			OrderMenu orderMenuDummy = oderService.saveOrder(req);
			Menu menuDummy = orderMenuDummy.getMenu();
			Customer customerDummy = orderMenuDummy.getCustomer();
			
			Menu menu = new Menu(menuDummy.getName(), null, null, null, null, null, null, null, null);
			menu.setId(menuDummy.getId());
			
			Customer customer = new Customer(customerDummy.getRef(), customerDummy.getTableDetail(), null, null, null, null, null, null);
			
			OrderMenu orderMenu = new OrderMenu(menu, orderMenuDummy.getCreatedDateTime(), null, null,
												orderMenuDummy.getStatus(), orderMenuDummy.getAmount(), orderMenuDummy.getIsTakeHome(), 
												null, null, orderMenuDummy.getComment(), customer);
			orderMenu.setId(orderMenuDummy.getId());
			
			LOG.debug("Call Broker");			
			template.convertAndSend("/topic/order", orderMenu);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/cancelOrder")
	public CommonCriteriaResp cancelOrder(@QueryParam("id") Long id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {			
			LOG.debug(id);
			
			oderService.setCancel(id, true);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/uncancelOrder")
	public CommonCriteriaResp uncancelOrder(@QueryParam("id") Long id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {			
			LOG.debug(id);
			
			oderService.setCancel(id, false);
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
	
	@GET
	@Path("/changeOrderStatus")
	public OrderSearchCriteriaResp changeOrderStatus(@QueryParam("ids") List<String> ids, @QueryParam("status") Integer status) {
		LOG.debug("Start");
		OrderSearchCriteriaResp resp = null;
		
		try {			
			LOG.debug(ids + " status: " + status);
			oderService.changeStatus(ids, status);
			
			resp = searchOrder();
		} catch (Exception e) {
			resp = new OrderSearchCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}
