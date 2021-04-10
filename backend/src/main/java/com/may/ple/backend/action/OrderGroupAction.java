package com.may.ple.backend.action;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
import com.may.ple.backend.entity.Receiver;
import com.may.ple.backend.service.OrderGroupService;
import com.may.ple.backend.service.OrderService;
import com.may.ple.backend.service.ReceiverService;
import com.may.ple.backend.utils.OrderNumberUtil;

@Component
@Path("orderGroup")
public class OrderGroupAction {
	private static final Logger LOG = Logger.getLogger(OrderGroupAction.class.getName());
	private OrderGroupService service;
	private OrderService orderService;
	private ReceiverService receiverService;

	@Autowired
	public OrderGroupAction(OrderGroupService service, OrderService orderService, ReceiverService receiverService) {
		this.service = service;
		this.orderService = orderService;
		this.receiverService = receiverService;
	}

	@POST
	@Path("/proceed")
	@Produces(MediaType.APPLICATION_JSON)
	public OrderCriteriaResp proceed(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			LOG.debug(req);
			service.proceed(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/getData")
	@Produces(MediaType.APPLICATION_JSON)
	public OrderCriteriaResp getData(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			LOG.debug(req);

			if(req.getTab().equals("0")) {
				List<String> receiverIds = req.getReceiverIds();
				String tabs[] = OrderNumberUtil.tabIndex;
				Map<String, Object> totalSum = new HashMap<>();
				Map<String, Object> data, innerData, subTotal;
				String tab, tabKey;
				Double sumPrice;

				for (int i = 0; i < tabs.length; i++) {
					tab = tabs[i];

					req.setTab(tab);
					data = service.getData(req, false);

					for (String recId : receiverIds) {
						if(!data.containsKey(recId)) continue;

						innerData = (Map)data.get(recId);
						sumPrice = (Double)innerData.get("sumPrice");
						tabKey = "eachPrice_" + tab;

						if(totalSum.containsKey(recId)) {
							subTotal = (Map)totalSum.get(recId);
							subTotal.put("sumPrice", (Double)subTotal.get("sumPrice") + sumPrice);
							subTotal.put(tabKey, sumPrice);
						} else {
							subTotal = new HashMap<>();
							subTotal.put("sumPrice", sumPrice);
							subTotal.put(tabKey, sumPrice);
							totalSum.put(recId, subTotal);
						}
					}
				}

				resp.setDataMap(totalSum);
				Map restrictedOrderMap = orderService.prepareRestrictedNumber(orderService.getRestrictedOrder(req), false);
				resp.setRestrictedOrder(restrictedOrderMap);
			} else {
				resp.setDataMap(service.getData(req, false));
			}
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/moveByPrice")
	public OrderCriteriaResp moveByPrice(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			service.moveByPrice(req);
			resp.setDataMap(service.getData(req, false));
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp.setStatusCode(1000);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/export")
	public Response export(final OrderCriteriaReq req) {
		LOG.debug("Export");

		try {
			final Receiver receiver = receiverService.getReceiverById(req.getReceiverId(), req.getDealerId());

			ResponseBuilder response = Response.ok(new StreamingOutput() {

				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					ByteArrayInputStream in = null;
					OutputStream out = null;
					try {
						byte[] data = null;

						if(req.getType().intValue() == 1) {
							data = service.exportData(req, receiver);
						} else if(req.getType().intValue() == 2) {
							data = service.export3Transform(req, receiver);
						}

						in = new ByteArrayInputStream(data);
						out = new BufferedOutputStream(os);
						int bytes;

						while ((bytes = in.read()) != -1) {
							out.write(bytes);
						}
					} catch (Exception e) {
						LOG.error(e.toString(), e);
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

}
