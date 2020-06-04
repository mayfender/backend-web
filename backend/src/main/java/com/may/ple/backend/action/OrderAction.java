package com.may.ple.backend.action;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.catalina.util.URLEncoder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.OrderCriteriaReq;
import com.may.ple.backend.criteria.OrderCriteriaResp;
import com.may.ple.backend.entity.OrderName;
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
			
			List<Map> periods = service.getPeriod();
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
			
			Map sumOrderTotal = service.getSumOrderTotal(null, req.getPeriodId(), req.getUserId());
			Double sumOrderTotalAll = (Double)sumOrderTotal.get("totalPrice") + Double.valueOf(sumOrderTotal.get("todPrice").toString());
			resp.setTotalPriceSumAll(sumOrderTotalAll);
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
			List<Map> periods = service.getPeriod();
			
			if(periods == null || periods.size() == 0 || userId == null) return resp;
			
			String periodId = periods.get(0).get("_id").toString();
			resp.setOrderNameLst(service.getOrderNameByPeriod(userId, periodId));
			resp.setPeriods(periods);
			
			Map sumOrderTotal = service.getSumOrderTotal(null, periodId, userId);
			if(sumOrderTotal != null) {				
				Double sumOrderTotalAll = (Double)sumOrderTotal.get("totalPrice") + Double.valueOf(sumOrderTotal.get("todPrice").toString());
				resp.setTotalPriceSumAll(sumOrderTotalAll);
			}
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
				types.add(16);
			} else if(req.getTab().equals("2")) {
				types.add(2);
				types.add(21);
			} else if(req.getTab().equals("3")) {
				types.add(3);
				types.add(31);				
			} else if(req.getTab().equals("4")) {
				types.add(4);				
			} else if(req.getTab().equals("5")) {
//				types.add(131);	
				types.add(13);	
				types.add(14);	
				types.add(15);
			}
			
			List<Map> sumOrderLst = service.getSumOrder(req.getTab(), types, req.getOrderName(), req.getPeriodId(), req.getUserId());
			resp.setOrderData(sumOrderLst);
			
			Double totalPriceSum = 0.0;
			for (int i = 0; i < sumOrderLst.size(); i++) {
				totalPriceSum += (Double)sumOrderLst.get(i).get("totalPrice");
			}
			
			resp.setTotalPriceSum(totalPriceSum);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/getSumOrderTotal")
	public OrderCriteriaResp getSumOrderTotal(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();
		
		try {
			Map sumOrderTotal = service.getSumOrderTotal(req.getOrderName(), req.getPeriodId(), req.getUserId());
			
			if(sumOrderTotal != null) {
				Double sumOrderTotalAll = (Double)sumOrderTotal.get("totalPrice") + Double.valueOf(sumOrderTotal.get("todPrice").toString());
				resp.setTotalPriceSumAll(sumOrderTotalAll);				
			}
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/getOrderName")
	public OrderCriteriaResp getOrderName(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();
		
		try {
			OrderName orderName = service.getOrderName(req.getUserId(), req.getName());			
			resp.setOrderName(orderName);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/export")
	public Response export(final OrderCriteriaReq req) {
		LOG.debug("Export");
		
		try {
			ResponseBuilder response = Response.ok(new StreamingOutput() {
				
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					ByteArrayInputStream in = null;
					OutputStream out = null;
					try {
						byte[] data = service.exportData(req.getPeriodId(), req.getUserId(), req.getPeriodDate());
						
						in = new ByteArrayInputStream(data);
						out = new BufferedOutputStream(os);
						int bytes;
						
						while ((bytes = in.read()) != -1) {
							out.write(bytes);
						}
					} catch (Exception e) {
						LOG.error(e.toString());
					} finally {
						if(in != null) in.close();
						if(out != null) out.close();
					}
				}
			});
			
			String fileName = String.format(new Locale("th", "TH"), "%1$td %1$tb %1$tY", req.getPeriodDate()) + ".zip";
			response.header("fileName", new URLEncoder().encode(fileName));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	@POST
	@Path("/saveResult")
	public OrderCriteriaResp saveResult(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();
		
		try {
			service.saveResult(req);
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/checkResult")
	public OrderCriteriaResp checkResult(@QueryParam("periodId")String periodId) {
		LOG.debug("Start");
		OrderCriteriaResp resp;
		
		try {
			resp = service.checkResult(periodId);
		} catch (Exception e) {
			resp = new OrderCriteriaResp(1000);
			LOG.error(e.toString(), e);
			throw e;
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getOrderNameByPeriod")
	public OrderCriteriaResp getOrderNameByPeriod(@QueryParam("periodId")String periodId, @QueryParam("userId")String userId) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();
		
		try {
			resp.setOrderNameLst(service.getOrderNameByPeriod(userId, periodId));
		} catch (Exception e) {
			resp = new OrderCriteriaResp(1000);
			LOG.error(e.toString(), e);
			throw e;
		}
		
		LOG.debug("End");
		return resp;
	}

}
