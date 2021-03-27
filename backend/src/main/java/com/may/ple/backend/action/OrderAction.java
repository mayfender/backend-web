package com.may.ple.backend.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.may.ple.backend.action.websocket.NotifyController;
import com.may.ple.backend.criteria.OrderCriteriaReq;
import com.may.ple.backend.criteria.OrderCriteriaResp;
import com.may.ple.backend.criteria.UserSearchCriteriaReq;
import com.may.ple.backend.entity.OrderName;
import com.may.ple.backend.entity.PriceList;
import com.may.ple.backend.entity.SendRound;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.OrderService;
import com.may.ple.backend.service.ReceiverService;
import com.may.ple.backend.service.SendRoundService;
import com.may.ple.backend.service.UserService;
import com.may.ple.backend.utils.OrderNumberUtil;

@Component
@Path("order")
public class OrderAction {
	private static final Logger LOG = Logger.getLogger(OrderAction.class.getName());
	private OrderService service;
	private ReceiverService receiverService;
	private UserService userService;
	private SendRoundService sRService;
	private NotifyController notifyWs;

	@Autowired
	public OrderAction(OrderService service, ReceiverService receiverService, UserService userService, SendRoundService sRService, NotifyController notifyWs) {
		this.notifyWs = notifyWs;
		this.receiverService = receiverService;
		this.userService = userService;
		this.sRService = sRService;
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
//			service.saveOrderNew(req);

			req.setCreatedDateTime(null);
			req.setOrderName(req.getName());
			resp = getData(req);
			resp.setOrderNameLst(service.getOrderNameByPeriod(req.getUserId(), req.getPeriodId(), req.getDealerId(), null));
		} catch (CustomerException e) {
			resp.setStatusCode(1001);
			LOG.error(e.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/checkOrderTime")
	@Produces(MediaType.APPLICATION_JSON)
	public OrderCriteriaResp checkOrderTime(OrderCriteriaReq req) {
		OrderCriteriaResp resp = new OrderCriteriaResp();
		try {
			int userRoleId = service.getUserRoleId();
			Calendar date = Calendar.getInstance();

			//---: Check Send Round
			date.set(Calendar.SECOND, 0);
			Date nowNoSec = date.getTime();

			Calendar limitedTime = Calendar.getInstance();
			date.setTime(req.getPeriodDateTime());
			List<SendRound> sendRoundList = sRService.getDataList(true, req.getDealerId());
			Date sendRoundDateTime;
			boolean isOverOrderTime = true;

			for (SendRound sendRound : sendRoundList) {
				limitedTime.setTime(sendRound.getLimitedTime());
				date.set(Calendar.HOUR_OF_DAY, limitedTime.get(Calendar.HOUR_OF_DAY));
				date.set(Calendar.MINUTE, limitedTime.get(Calendar.MINUTE));
				date.set(Calendar.SECOND, limitedTime.get(Calendar.SECOND));
				sendRoundDateTime = date.getTime();

				if(nowNoSec.before(sendRoundDateTime)) {
					resp.setSendRoundDateTime(sendRoundDateTime);
					resp.setSendRoundMsg(sendRound.getName());
					isOverOrderTime = false;
					break;
				}
			}

			//---: userRoleId = 3 = ADMIN
			if(userRoleId != 3 && isOverOrderTime) {
				LOG.info("Over to send order.");
				resp.setIsOverOrderTime(isOverOrderTime);
				return resp;
			}
			//---: Check Send Round
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/saveOrder2")
	@Produces(MediaType.APPLICATION_JSON)
	public OrderCriteriaResp saveOrder2(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			LOG.debug(req);

			resp = checkOrderTime(req);
			if(resp.getIsOverOrderTime() != null) {
				return resp;
			}

			Calendar date = Calendar.getInstance();
			Date now = date.getTime();

			req.setCreatedDateTime(now);
			req.setDeviceId(2); // Mobile

			Map<String, Integer> restrictList = service.saveOrder2(req);

			resp.setRestrictList(restrictList);
			resp.setCreatedDateTime(now);

			//---: WS notify
			Users user = userService.getUserByName(SecurityContextHolder.getContext().getAuthentication().getName());
			notifyWs.pinNumNotify(req.getDealerId(), user.getShowname());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/editDelete")
	@Produces(MediaType.APPLICATION_JSON)
	public OrderCriteriaResp editDelete(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			LOG.debug(req);

			int userRoleId = service.getUserRoleId();

			//---: Check time allowance
			//---: userRoleId = 1 = AGENT
			if(req.getDeviceId().intValue() == 2 && userRoleId == 1) {
				Calendar date = Calendar.getInstance();
				Date now = date.getTime();

				if(req.getDeleteGroup() == null) {
					date.setTime(req.getCreatedDateTimeDelete());
				} else {
					date.setTime(req.getDeleteGroup());
				}
				date.set(Calendar.SECOND, 0);
				Date itemDateTime = date.getTime();

				date.setTime(req.getPeriodDateTime());
				Calendar limitedTime = Calendar.getInstance();
				Date sendRoundDateTime, itemRoundDateTime = null;
				List<SendRound> sendRoundList = sRService.getDataList(true, req.getDealerId());
				for (SendRound sendRound : sendRoundList) {
					limitedTime.setTime(sendRound.getLimitedTime());
					date.set(Calendar.HOUR_OF_DAY, limitedTime.get(Calendar.HOUR_OF_DAY));
					date.set(Calendar.MINUTE, limitedTime.get(Calendar.MINUTE));
					date.set(Calendar.SECOND, limitedTime.get(Calendar.SECOND));
					sendRoundDateTime = date.getTime();

					if(itemDateTime.before(sendRoundDateTime)) {
						itemRoundDateTime = sendRoundDateTime;
						break;
					}
				}

				if(now.after(itemRoundDateTime)) {
					resp.setNotAllowRemove(true);
					return resp;
				}
			}

			//---:
			if(req.getDeleteGroup() == null) {
				service.editDelete(req);
			} else {
				service.deleteGroup(req);
			}

			//---:
			resp = getData(req);

			if(req.getDeviceId().intValue() == 2) {
				resp.setCreatedDateGroup(service.getOrderGroupByCreatedDate(req.getUserId(), req.getPeriodId(), req.getDealerId()));
				resp.setOrderNameLst(service.getOrderNameByPeriod(req.getUserId(), req.getPeriodId(), req.getDealerId(), null));
			} else {
				resp.setOrderNameLst(service.getOrderNameByPeriod(req.getUserId(), req.getPeriodId(), req.getDealerId(), null));
			}
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
	public OrderCriteriaResp getPeriod(
			@QueryParam("userId")String userId,
			@QueryParam("dealerId")String dealerId,
			@QueryParam("isGetUsers")Boolean isGetUsers) {

		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			List<Map> periods = service.getPeriod();

			if(periods == null || periods.size() == 0) return resp;

			resp.setPeriods(periods);
			resp.setReceiverList(receiverService.getReceiverList(true, dealerId));

			if(isGetUsers != null && isGetUsers) {
				LOG.info("Get Users");
				UserSearchCriteriaReq req = new UserSearchCriteriaReq();
				req.setDealerId(dealerId);
				resp.setUsers(userService.getUsers(req));
			}
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/getData")
	public OrderCriteriaResp getData(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			List<String> receiverIds = req.getReceiverIds();

			if(receiverIds == null) {
				resp = getSumOrder(req);
			} else {
				Map<String, Object> data = new HashMap<>();
				for (String recId : receiverIds) {
					req.setReceiverId(recId);
					data.put(recId, getSumOrder(req));
				}
				resp.setDataMap(data);

				if(req.getTab().equals("0")) {
					Map restrictedOrderMap = service.prepareRestrictedNumber(service.getRestrictedOrder(req), false);
					resp.setRestrictedOrder(restrictedOrderMap);
				}
			}
			if(req.getDeviceId().intValue() == 2) {
				resp.setCreatedDateGroup(service.getOrderGroupByCreatedDate(req.getUserId(), req.getPeriodId(), req.getDealerId()));
				resp.setOrderNameLst(service.getOrderNameByPeriod(req.getUserId(), req.getPeriodId(), req.getDealerId(), null));
			} else {
				resp.setOrderNameLst(service.getOrderNameByPeriod(req.getUserId(), req.getPeriodId(), req.getDealerId(), req.getUserRole()));
				if(req.getTab().equals("0")) {
					List<Map> orderData = resp.getOrderData();

					if(req.getIsAllOrder() == null || !req.getIsAllOrder()) {
						if(orderData.size() > 10) {
							resp.setOrderData(orderData.subList(orderData.size()-10, orderData.size()));
						}
					}
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
				String group = "";

				if(req.getChkBoxType().isBon3()) {
					group += "1";
				}
				if(req.getChkBoxType().isBon2()) {
					group += "2";
				}
				if(req.getChkBoxType().isLang2()) {
					group += "3";
				}
				if(req.getChkBoxType().isLoy()) {
					group += "4";
				}
				if(req.getChkBoxType().isPair4()) {
					group += "5";
				}
				if(req.getChkBoxType().isPair5()) {
					group += "6";
				}
				if(req.getChkBoxType().isRunBon()) {
					group += "7";
				}
				if(req.getChkBoxType().isRunLang()) {
					group += "8";
				}

				List<Integer> typeLst = service.getGroup(group, true);
				Map<String, Object> dataMap = service.getDataOnTL(
					req.getPeriodId(), req.getUserId(), req.getOrderName(), typeLst,
					req.getReceiverId(), new Sort("createdDateTime"), req.getDealerId(),
					req.getCreatedDateTime(), true, req.getUserRole()
				);
				resp.setOrderData((List<Map>)dataMap.get("orderLst"));
				resp.setTotalPriceSumAll((double)dataMap.get("sumOrderTotal"));
			} else {
				List<Integer> typeLst = service.getGroup(req.getTab(), false);

				List<Map> sumOrderLst = service.getSumOrder(
					req.getTab(), typeLst, req.getOrderName(), req.getPeriodId(), req.getUserId(),
					req.getReceiverId(), req.getDealerId(), req.getUserRole(), null
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
	@Path("/changePrice")
	public OrderCriteriaResp changePrice(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			service.changePrice(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/getSumPaymentAll")
	public OrderCriteriaResp getSumPaymentAll(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			//---:
			Map<String, Object> priceData = getPriceData(req);

			//---: Administrator: Get order name List.
			OrderCriteriaReq reqData = (OrderCriteriaReq)req.clone();
			reqData.setTab("1");
			reqData.setUserRole(3);
			OrderCriteriaResp data = getData(reqData);
			List<String> orderNameLst = data.getOrderNameLst();

			Map<String, Object> sumPaymentImpl = getSumPaymentImpl(reqData, orderNameLst, true, priceData);
			Map<String, Object> resultGroup = new HashMap<>();
			resultGroup.put("admin", sumPaymentImpl.get("resultList"));

			//---: Customer: Get order name List.
			OrderCriteriaResp periodData = getPeriod(null, req.getDealerId(), true);
			List<Users> users = periodData.getUsers();
			orderNameLst.clear();
			for (Users u : users) {
				if(u.getRoleId() != 1) continue;

				orderNameLst.add(u.getId() + "," + u.getShowname());
			}

			reqData.setUserRole(1);
			sumPaymentImpl = getSumPaymentImpl(reqData, orderNameLst, false, priceData);
			resultGroup.put("customer", sumPaymentImpl.get("resultList"));

			resp.setPaymentData(resultGroup);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/getSumPaymentByOne")
	public OrderCriteriaResp getSumPaymentByOne(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			String[] tabs = new String[] {"1", "2", "3", "4", "41", "42", "43", "44", "5"};
			Map<String, Double> data = new HashMap<>();

			for (String tab : tabs) {
				req.setTab(tab);
				OrderCriteriaResp sumOrder = getSumOrder(req);
				data.put(tab, sumOrder.getTotalPriceSum());
			}
			resp.setTotalPriceSumAllMap(data);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	/*@POST
	@Path("/getSumOrderTotal")
	public OrderCriteriaResp getSumOrderTotal(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			List<Integer> typeLst;

			if(req.getTypeLst() == null) {
				String group = "";
				if(req.getChkBoxType().isBon3()) {
					group += "1";
				}
				if(req.getChkBoxType().isBon2()) {
					group += "2";
				}
				if(req.getChkBoxType().isLang2()) {
					group += "3";
				}
				if(req.getChkBoxType().isLoy()) {
					group += "4";
				}
				typeLst = service.getGroup(group, true);
			} else {
				typeLst = req.getTypeLst();
			}

			Map sumOrderTotal = service.getSumOrderTotal(req.getOrderName(), req.getPeriodId(), req.getUserId(), req.getReceiverId(), typeLst);

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
	}*/

	@POST
	@Path("/getNames")
	public OrderCriteriaResp getNames(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			Map<String, Object> customerNameMap = service.getCustomerName(service.getUserRoleId(), req.getUserId(), req.getDealerId());
			if(customerNameMap != null && customerNameMap.get("names") != null) {
				List<String> names = new ArrayList<>();
				List<Map> namesLstMap = (List<Map>)customerNameMap.get("names");

				for (Map name : namesLstMap) {
					if((boolean)name.get("enabled")) {
						names.add(name.get("name").toString());
					}
				}
				Collections.sort(names);
				resp.setOrderNameLst(names);
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
			OrderName orderName = service.getOrderName(req.getUserId(), req.getName(), req.getDealerId());
			resp.setOrderName(orderName);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
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
	@Path("/updateRestricted")
	public OrderCriteriaResp updateRestricted(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			service.updateRestricted(req);
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
		OrderCriteriaResp resp;

		try {
			boolean isRestricted = false;
			try {
				service.moveToReceiver(req);
			} catch (CustomerException e) {
				isRestricted = true;
				LOG.error(e.toString());
			}

			resp = getData(req);

			if(isRestricted) {
				resp.setStatusCode(1001);
			}
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new OrderCriteriaResp(1000);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/moveToReceiverWithCond")
	public OrderCriteriaResp moveToReceiverWithCond(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			List<Integer> types = service.getGroup(req.getTab(), false);

			LOG.debug("call moveToReceiverWithCond");
			int movedNum = service.moveToReceiverWithCond(req, types);

			Map<String, Object> data = new HashMap<>();
			OrderCriteriaResp orderResp;

			for (int i = 0; i < 2; i++) {
				req.setReceiverId(i == 0 ? req.getMoveFromId() : req.getMoveToId());

				LOG.debug("getSumOrder");
				orderResp = getSumOrder(req);

				orderResp.getOrderData();
				orderResp.getTotalPriceSum();
				data.put(req.getReceiverId(), orderResp);
			}

			resp.setDataMap(data);
			resp.setMovedNum(movedNum);
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp.setStatusCode(1000);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/getPinNum")
	public OrderCriteriaResp getPinNum(OrderCriteriaReq req) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			resp.setPinNums(service.getPinNum(req));
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp.setStatusCode(1000);
		}

		LOG.debug("End");
		return resp;
	}

	@GET
	@Path("/checkResult")
	public OrderCriteriaResp checkResult(@QueryParam("periodId")String periodId, @QueryParam("dealerId")String dealerId, @QueryParam("userId")String userId) {
		LOG.debug("Start");
		OrderCriteriaResp resp;

		try {
			resp = service.checkResult(periodId, dealerId, userId);
		} catch (Exception e) {
			resp = new OrderCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@GET
	@Path("/getOrderNameByPeriod")
	public OrderCriteriaResp getOrderNameByPeriod(@QueryParam("periodId")String periodId, @QueryParam("userId")String userId, @QueryParam("dealerId")String dealerId) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			resp.setOrderNameLst(service.getOrderNameByPeriod(userId, periodId, dealerId, null));
		} catch (Exception e) {
			resp = new OrderCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@GET
	@Path("/getOrderNumProb")
	public OrderCriteriaResp getOrderNumProb(@QueryParam("orderNumber")String orderNumber) {
		LOG.debug("Start");
		OrderCriteriaResp resp = new OrderCriteriaResp();

		try {
			resp.setOrderNumberList(OrderNumberUtil.getOrderNumProb(orderNumber));
		} catch (Exception e) {
			resp = new OrderCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	private Map<String, Object> getPriceData(OrderCriteriaReq req) {
		try {
			//---: Get Price List.
			List<PriceList> priceList = receiverService.getPriceList(true, req.getDealerId());
			//---: Get All Customer Name.
			List<Map> customerNameAll = service.getCustomerNameAll(req.getDealerId());
			List<Map> names;
			Map<String, Object> priceData = new HashMap<>();
			for (Map customerMap : customerNameAll) {
				if(customerMap.get("userGroup").equals("3")) {
					names = (List)customerMap.get("names");
					for (Map nameMap : names) {
						for (PriceList price : priceList) {
							if(nameMap.get("priceId") == null) continue;
							if(price.getId().equals(nameMap.get("priceId").toString())) {
								priceData.put(nameMap.get("name").toString(), price);
							}
						}
					}
				} else {
					for (PriceList price : priceList) {
						if(customerMap.get("priceId") == null) continue;
						if(price.getId().equals(customerMap.get("priceId").toString())) {
							priceData.put(customerMap.get("userGroup").toString(), price);
						}
					}
				}
			}
			return priceData;
		} catch (Exception e) {
			LOG.error(e.toString());;
			throw e;
		}
	}

	private Map<String, Object> getSumPaymentImpl(OrderCriteriaReq reqData, List<String> orderNameLst, boolean byName, Map<String, Object> priceData) {
		try {
			Map<String, Object> result = new HashMap<>();
			List<Map<String, Object>> resultList = new ArrayList<>();
			List<Map<String, String>> typeTitleList = getTypeTitleList();

			Double val, sum, sumDiscount;
			Map<String, Object> firstPriceList;
			Map<String, Double> totalPriceSumAllMap;
			OrderCriteriaResp sumPaymentByOne;
			Map.Entry<String, Object> keyObj;
			Map<String, Object> subResult;
			String userId = null;
			PriceList priceList;
			Object percent;

			for (String name : orderNameLst) {
				subResult = new HashMap<>();
				firstPriceList = null;

				if(byName) {
					reqData.setOrderName(name);
					reqData.setUserId(null);
					priceList = (PriceList)priceData.get(name);
				} else {
					userId = name.split(",")[0];
					name = name.split(",")[1];
					reqData.setUserId(userId);
					reqData.setOrderName(null);
					priceList = (PriceList)priceData.get(userId);
				}

				if(priceList != null) {
					//---: Get PriceList first key
					keyObj = (Map.Entry)priceList.getPriceData().entrySet().iterator().next();
					firstPriceList = (Map)keyObj.getValue();
					subResult.put("price", priceList.getId());
				}

				sumPaymentByOne = getSumPaymentByOne(reqData);
				totalPriceSumAllMap = sumPaymentByOne.getTotalPriceSumAllMap();
				sum = 0.0;
				sumDiscount = 0.0;
				for (Map<String, String> titleMap : typeTitleList) {
					val = totalPriceSumAllMap.get(titleMap.get("type"));
					sum += val;
					if(firstPriceList != null && (percent = firstPriceList.get(titleMap.get("percent"))) != null) {
						sumDiscount += ((Double)percent / 100) * val;
					}
				}

				if(sum == 0) continue;

				subResult.put("id", userId);
				subResult.put("name", name);
				subResult.put("sum", sum);
				subResult.put("sumDiscount", sumDiscount);
				subResult.put("isCustomer", byName ? false : true);

				resultList.add(subResult);
			}

			result.put("resultList", resultList);

			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private List<Map<String, String>> getTypeTitleList() {
		List<Map<String, String>> typeTitleList = new ArrayList<>();
		Map<String, String> hashMap = null;

		hashMap = new HashMap<>();
		hashMap.put("type", "1"); hashMap.put("title", "รวม 3 บน"); hashMap.put("percent", "percentBon3");
		typeTitleList.add(hashMap);

		hashMap = new HashMap<>();
		hashMap.put("type", "5"); hashMap.put("title", "รวมโต๊ด"); hashMap.put("percent", "percentTod");
		typeTitleList.add(hashMap);

		hashMap = new HashMap<>();
		hashMap.put("type", "2"); hashMap.put("title", "รวม 2 บน"); hashMap.put("percent", "percentBon2");
		typeTitleList.add(hashMap);

		hashMap = new HashMap<>();
		hashMap.put("type", "3"); hashMap.put("title", "รวม 2 ล่าง"); hashMap.put("percent", "percentLang2");
		typeTitleList.add(hashMap);

		hashMap = new HashMap<>();
		hashMap.put("type", "4"); hashMap.put("title", "รวมลอย"); hashMap.put("percent", "percentLoy");
		typeTitleList.add(hashMap);

		hashMap = new HashMap<>();
		hashMap.put("type", "41"); hashMap.put("title", "รวมแพ 4"); hashMap.put("percent", "percentPare4");
		typeTitleList.add(hashMap);

		hashMap = new HashMap<>();
		hashMap.put("type", "42"); hashMap.put("title", "รวมแพ 5"); hashMap.put("percent", "percentPare5");
		typeTitleList.add(hashMap);

		hashMap = new HashMap<>();
		hashMap.put("type", "43"); hashMap.put("title", "รวมวิ่งบน"); hashMap.put("percent", "percentRunBon");
		typeTitleList.add(hashMap);

		hashMap = new HashMap<>();
		hashMap.put("type", "44"); hashMap.put("title", "รวมวิ่งล่าง"); hashMap.put("percent", "percentRunLang");
		typeTitleList.add(hashMap);

		return typeTitleList;
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
