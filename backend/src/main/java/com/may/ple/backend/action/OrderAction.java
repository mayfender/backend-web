package com.may.ple.backend.action;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.may.ple.backend.entity.Receiver;
import com.may.ple.backend.service.OrderService;
import com.may.ple.backend.service.SettingService;

@Component
@Path("order")
public class OrderAction {
	private static final Logger LOG = Logger.getLogger(OrderAction.class.getName());
	private OrderService service;
	private SettingService settingService;
	
	@Autowired
	public OrderAction(OrderService service, SettingService settingService) {
		this.service = service;
		this.settingService = settingService;
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
			
			Map sumOrderTotal = service.getSumOrderTotal(null, req.getPeriodId(), req.getUserId(), null);
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
	public OrderCriteriaResp getPeriod(@QueryParam("userId")String userId, @QueryParam("isAll")Boolean isAll) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();
		
		try {
			List<Map> periods = service.getPeriod();
			
			if(periods == null || periods.size() == 0 || userId == null) return resp;
			
			String periodId = periods.get(0).get("_id").toString();
			resp.setOrderNameLst(service.getOrderNameByPeriod(userId, periodId));
			resp.setPeriods(periods);
			
			if(isAll != null && isAll) {
				List<Receiver> receiverList = settingService.getReceiverList(true);	
				Map<String, Double> totalMap = new HashMap<>();
				Double sumOrderTotalAll;
				
				for (Receiver receiver : receiverList) {
					Map sumOrderTotal = service.getSumOrderTotal(null, periodId, userId, receiver.getId());
					if(sumOrderTotal != null) {				
						sumOrderTotalAll = (Double)sumOrderTotal.get("totalPrice") + Double.valueOf(sumOrderTotal.get("todPrice").toString());
						totalMap.put(receiver.getId(), sumOrderTotalAll);
					}
				}
				resp.setTotalPriceSumAllMap(totalMap);
			} else {
				Map sumOrderTotal = service.getSumOrderTotal(null, periodId, userId, null);
				if(sumOrderTotal != null) {				
					Double sumOrderTotalAll = (Double)sumOrderTotal.get("totalPrice") + Double.valueOf(sumOrderTotal.get("todPrice").toString());
					resp.setTotalPriceSumAll(sumOrderTotalAll);
				}				
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
			if(req.getTab().equals("0")) {
				List<Integer> typeLst = new ArrayList<>();
				
				if(req.getChkBoxType().isBon3()) {
					typeLst.add(1);
					typeLst.add(11);
					typeLst.add(12);
					typeLst.add(13);
					typeLst.add(14);
				} 
				if(req.getChkBoxType().isBon2()) {
					
					typeLst.add(2);
					typeLst.add(21);
				}
				if(req.getChkBoxType().isLang2()) {
					typeLst.add(3);
					typeLst.add(31);					
				}
				if(req.getChkBoxType().isLoy()) {
					typeLst.add(4);										
				}
				resp.setOrderData(
					service.getDataOnTL(req.getPeriodId(), req.getUserId(), req.getOrderName(), typeLst, req.getReceiverId())
				);
			} else {
				List<Integer> types = new ArrayList<>();
				if(req.getTab().equals("1")) {
					types.add(1);
					types.add(11);
					types.add(12);
					types.add(13);
					types.add(14);
				} else if(req.getTab().equals("2")) {
					types.add(2);
					types.add(21);
				} else if(req.getTab().equals("3")) {
					types.add(3);
					types.add(31);				
				} else if(req.getTab().equals("4")) {
					types.add(4);				
				} else if(req.getTab().equals("5")) {
					types.add(13);
					types.add(14);
				}
				
				List<Map> sumOrderLst = service.getSumOrder(
					req.getTab(), types, req.getOrderName(), req.getPeriodId(), req.getUserId(), req.getReceiverId()
				);
				
				resp.setOrderData(sumOrderLst);
				
				Double totalPriceSum = 0.0;
				for (int i = 0; i < sumOrderLst.size(); i++) {
					totalPriceSum += (Double)sumOrderLst.get(i).get("totalPrice");
				}
				
				resp.setTotalPriceSum(totalPriceSum);
			}
			
			resp.setReceiverId(req.getReceiverId());
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
			Map sumOrderTotal = service.getSumOrderTotal(req.getOrderName(), req.getPeriodId(), req.getUserId(), req.getReceiverId());
			
			if(sumOrderTotal != null) {
				Double sumOrderTotalAll = (Double)sumOrderTotal.get("totalPrice") + Double.valueOf(sumOrderTotal.get("todPrice").toString());
				resp.setTotalPriceSumAll(sumOrderTotalAll);				
			}
			
			resp.setReceiverId(req.getReceiverId());
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
			final Receiver receiver = settingService.getReceiverById(req.getReceiverId());
			
			ResponseBuilder response = Response.ok(new StreamingOutput() {
				
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					ByteArrayInputStream in = null;
					OutputStream out = null;
					try {
						byte[] data = service.exportData(req.getPeriodId(), req.getUserId(), req.getPeriodDate(), req.getReceiverId(), receiver);
						
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
			
			String fileName = receiver.getReceiverName() + "_" + String.format(new Locale("th", "TH"), "%1$td %1$tb %1$tY", req.getPeriodDate()) + ".zip";
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
			resp.setStatusCode(1000);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/moveToReceiver")
	public OrderCriteriaResp moveToReceiver(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();
		
		try {
			service.moveToReceiver(req);
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp.setStatusCode(1000);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/checkResult")
	public OrderCriteriaResp checkResult(@QueryParam("periodId")String periodId, @QueryParam("isAllReceiver")Boolean isAllReceiver) {
		LOG.debug("Start");
		OrderCriteriaResp resp;
		
		try {
			resp = service.checkResult(periodId, isAllReceiver);
		} catch (Exception e) {
			resp = new OrderCriteriaResp(1000);
			LOG.error(e.toString(), e);
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
		}
		
		LOG.debug("End");
		return resp;
	}
	
	/*@GET
	@Path("/getDataOnTL")
	public OrderCriteriaResp getDataOnTL(@QueryParam("periodId")String periodId, @QueryParam("userId")String userId) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();
		
		try {
			resp.setOrderData(service.getDataOnTL(periodId, userId));
		} catch (Exception e) {
			resp = new OrderCriteriaResp(1000);
			LOG.error(e.toString(), e);
			throw e;
		}
		
		LOG.debug("End");
		return resp;
	}*/

}
