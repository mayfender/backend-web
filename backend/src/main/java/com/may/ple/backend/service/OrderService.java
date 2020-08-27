package com.may.ple.backend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.may.ple.backend.bussiness.jasper.JasperReportEngine;
import com.may.ple.backend.constant.OrderTypeConstant;
import com.may.ple.backend.criteria.OrderCriteriaReq;
import com.may.ple.backend.criteria.OrderCriteriaResp;
import com.may.ple.backend.criteria.UserSearchCriteriaReq;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.Order;
import com.may.ple.backend.entity.OrderName;
import com.may.ple.backend.entity.Period;
import com.may.ple.backend.entity.Receiver;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ZipUtil;
import com.mongodb.BasicDBObject;

@Service
public class OrderService {
	private static final Logger LOG = Logger.getLogger(OrderService.class.getName());
	private ReceiverService receiverService;
	private UserService userService;
	private MongoTemplate template;
	private DbFactory dbFactory;
	@Value("${file.path.base}")
	private String basePath;

	@Autowired
	public OrderService(MongoTemplate template, ReceiverService receiverService, DbFactory dbFactory, UserService userService) {
		this.template = template;
		this.receiverService = receiverService;
		this.dbFactory = dbFactory;
		this.userService = userService;
	}

	public void savePeriod(OrderCriteriaReq req) {
		try {
			Period period = new Period();
			period.setPeriodDateTime(req.getPeriodDateTime());
			period.setCreatedDateTime(Calendar.getInstance().getTime());

			template.save(period, "period");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void saveOrder(OrderCriteriaReq req) throws Exception {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());

			Query query = Query.query(Criteria.where("enabled").is(true));
			query.with(new Sort("order"));
			query.fields().include("id");

			Receiver firstReceiver = dealerTemp.findOne(query, Receiver.class);

			LOG.debug("Start prepareRestrictedNumber");
			List<String> ids = new ArrayList<>();
			ids.add(firstReceiver.getId());

			OrderCriteriaReq reqRest = new OrderCriteriaReq();
			reqRest.setPeriodId(req.getPeriodId());
			reqRest.setReceiverIds(ids);

			Object restrictedOrderObj = prepareRestrictedNumber(getRestrictedOrder(reqRest), true).get(firstReceiver.getId());
			Map noPrice = null, halfPrice = null;
			if(restrictedOrderObj != null) {
				Map restrictedOrderMap = (Map)restrictedOrderObj;
				noPrice = (Map)restrictedOrderMap.get("noPrice");
				halfPrice = (Map)restrictedOrderMap.get("halfPrice");
			}
			LOG.debug("End prepareRestrictedNumber");

			List<Order> objLst = new ArrayList<>();
			List<String> orderNumProb = null;
			Integer parentType, childType;
			Double childPrice = null;

			if(req.getOrderNumber().length() == 2) {
				if(req.getBon() != null) {
					parentType = OrderTypeConstant.TYPE2.getId();
					if(req.getBonSw()) {
						orderNumProb = getOrderNumProb(req.getOrderNumber());
						if(orderNumProb.size() == 2) {
							parentType = OrderTypeConstant.TYPE21.getId();
						}
					} else {
						orderNumProb = new ArrayList<>();
						orderNumProb.add(req.getOrderNumber());
					}

					//---------
					objLst.addAll(prepareDbObj(orderNumProb, req.getName(), parentType,
							parentType, req.getBon(), req.getBon(), req.getUserId(), req.getPeriodId(),
							null, firstReceiver.getId(), noPrice, halfPrice));
				}
				if(req.getLang() != null) {
					parentType = OrderTypeConstant.TYPE3.getId();
					if(req.getLangSw()) {
						orderNumProb = getOrderNumProb(req.getOrderNumber());
						if(orderNumProb.size() == 2) {
							parentType = OrderTypeConstant.TYPE31.getId();
						}
					} else {
						orderNumProb = new ArrayList<>();
						orderNumProb.add(req.getOrderNumber());
					}

					//---------
					objLst.addAll(prepareDbObj(orderNumProb, req.getName(), parentType,
							parentType, req.getLang(), req.getLang(), req.getUserId(),
							req.getPeriodId(), null, firstReceiver.getId(), noPrice, halfPrice));
				}
			} else if(req.getOrderNumber().length() == 3 && req.getBon() != null) {
				boolean isTod = req.getTod() != null;
				parentType = childType = OrderTypeConstant.TYPE1.getId();

				if(req.getBonSw() || isTod) {
					orderNumProb = getOrderNumProb(req.getOrderNumber());

					if(req.getBonSw() && isTod) {
						parentType = OrderTypeConstant.TYPE14.getId();
						childPrice = req.getTod();
					} else if(req.getBonSw()) {
						childPrice = req.getBon();
						parentType = childType = OrderTypeConstant.TYPE11.getId();
					} else if(isTod) {
						parentType = OrderTypeConstant.TYPE13.getId();
						childType = OrderTypeConstant.TYPE131.getId();
						childPrice = req.getTod();
					}
				} else {
					orderNumProb = new ArrayList<>();
					orderNumProb.add(req.getOrderNumber());
				}

				//---------
				objLst.addAll(prepareDbObj(orderNumProb, req.getName(), parentType, childType,
						req.getBon(), childPrice, req.getUserId(), req.getPeriodId(),
						null, firstReceiver.getId(), noPrice, halfPrice));
			} else if(req.getOrderNumber().length() > 3 && req.getBon() != null) {
				orderNumProb = getOrderNumProbOver3(req.getOrderNumber());
				parentType = childType = OrderTypeConstant.TYPE12.getId();
				childPrice = req.getBon();

				//---------
				objLst.addAll(prepareDbObj(orderNumProb, req.getName(), parentType, childType,
						req.getBon(), childPrice, req.getUserId(), req.getPeriodId(),
						req.getOrderNumber(), firstReceiver.getId(), noPrice, halfPrice));
			}

			if(req.getLoy() != null) {
				if(req.getOrderNumber().length() == 1) {
					parentType = OrderTypeConstant.TYPE4.getId();
				} else if(req.getOrderNumber().length() == 4) {
					parentType = OrderTypeConstant.TYPE41.getId();
				} else {
					parentType = OrderTypeConstant.TYPE42.getId();
				}
				orderNumProb = new ArrayList<>();
				orderNumProb.add(req.getOrderNumber());

				objLst.addAll(prepareDbObj(orderNumProb, req.getName(), parentType,
						parentType, req.getLoy(), null, req.getUserId(), req.getPeriodId(),
						null, firstReceiver.getId(), noPrice, halfPrice));
			}

			if(req.getRunBon() != null) {
				parentType = OrderTypeConstant.TYPE43.getId();
				orderNumProb = new ArrayList<>();
				orderNumProb.add(req.getOrderNumber());

				objLst.addAll(prepareDbObj(orderNumProb, req.getName(), parentType,
						parentType, req.getRunBon(), null, req.getUserId(), req.getPeriodId(),
						null, firstReceiver.getId(), noPrice, halfPrice));
			}
			if(req.getRunLang() != null) {
				parentType = OrderTypeConstant.TYPE44.getId();
				orderNumProb = new ArrayList<>();
				orderNumProb.add(req.getOrderNumber());

				objLst.addAll(prepareDbObj(orderNumProb, req.getName(), parentType,
						parentType, req.getRunLang(), null, req.getUserId(), req.getPeriodId(),
						null, firstReceiver.getId(), noPrice, halfPrice));
			}

			dealerTemp.insert(objLst, "order");

			//---------------------------------------------------
			query = Query.query(Criteria.where("userId").is(new ObjectId(req.getUserId())));
			OrderName orderName = dealerTemp.findOne(query, OrderName.class, "orderName");

			if(orderName == null) {
				LOG.debug("Empty OrderName");
				List<Map> names = new ArrayList<>();
				Map<String, String> name = new HashMap<>();
				name.put("name", req.getName());
				names.add(name);

				orderName = new OrderName();
				orderName.setUserId(new ObjectId(req.getUserId()));
				orderName.setNames(names);

				dealerTemp.save(orderName);
			} else if(orderName != null) {
				LOG.debug("Existing OrderName");
				Update update = new Update();
				update.addToSet("names", new BasicDBObject("name", req.getName()));

				query = Query.query(Criteria.where("_id").is(new ObjectId(orderName.getId())));
				dealerTemp.updateFirst(query, update, "orderName");
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void saveOrder2(OrderCriteriaReq req) throws Exception {
		try {
			List<Map> ordList = req.getOrderList();
			String[] ordSetsplited, priceSetsplited;
			String orderNumber, priceSet, ordSet;
			Double price, priceDesc;
			Integer type;

			for (Map ord : ordList) {
				ordSet = (String)ord.get("orderNumberSet");
				type = (Integer)ord.get("type");

				//---: Get order number.
				ordSetsplited = ordSet.split("=");
				orderNumber = ordSetsplited[0];

				if(StringUtils.isBlank(orderNumber)) throw new Exception("OrderNumber cann't be empty.");

				//---: Get Price & Price Description.
				priceSet = ordSetsplited[1];
				priceSetsplited = priceSet.split("x");
				price = Double.valueOf(priceSetsplited[0]);

				if(price == null || price.intValue() < 1) throw new Exception("Price should be greater than 0 and not be empty {" + ordSet + "}");

				if(priceSetsplited.length == 2) {
					priceDesc = Double.valueOf(priceSetsplited[1]);
				} else if(priceSetsplited.length > 2) {
					throw new Exception("Not support this format {" + ordSet + "}");
				} else {
					priceDesc = null;
				}

				Date now = Calendar.getInstance().getTime();

				Map periodMap = getPeriod().get(0);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime((Date)periodMap.get("periodDateTime"));
				calendar.set(Calendar.HOUR_OF_DAY, 17);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				Date periodDateTime = calendar.getTime();

				if(now.after(periodDateTime)) throw new Exception("It's overtime for current period!!!.");

				//---: Prepare data
				req.setPeriodId(periodMap.get("_id").toString());
				List<OrderCriteriaReq> orderReqList = translateOrdNumber(req, orderNumber, type, price, priceDesc);

				//---: Save
				for (OrderCriteriaReq ordReq : orderReqList) {
					saveOrder(ordReq);
				}
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void editDelete(OrderCriteriaReq req) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());

			if(StringUtils.isBlank(req.getOrderNameUpdate())) {
				LOG.info("Delete");
				Criteria criteria1 = Criteria.where("_id").is(new ObjectId(req.getOrderId()));
				Criteria criteria2 = Criteria.where("parentId").is(new ObjectId(req.getOrderId()));

				Query query = Query.query(new Criteria().orOperator(criteria1, criteria2));
				dealerTemp.remove(query, "order");
			} else {
				LOG.info("Update Name");

				Criteria criteria1 = Criteria.where("_id").is(new ObjectId(req.getOrderId()));
				Criteria criteria2 = Criteria.where("parentId").is(new ObjectId(req.getOrderId()));

				Query query = Query.query(new Criteria().orOperator(criteria1, criteria2));

				Update update = new Update();
				update.set("name", req.getOrderNameUpdate());

				dealerTemp.updateMulti(query, update, "order");
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List<Map> getPeriod() {
		try {
			Query query = new Query();
			query.with(new Sort(Sort.Direction.DESC, "periodDateTime"));
			query.limit(10);

			return template.find(query, Map.class, "period");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public Map getRestrictedOrder(OrderCriteriaReq req) {
		try {
			Criteria criteria = Criteria.where("_id").is(new ObjectId(req.getPeriodId()));

			List<Object> orList = new ArrayList<>();
			List<Object> eqList;
			for (String recId : req.getReceiverIds()) {
				eqList = new ArrayList<>();
				eqList.add("$$restrictedOrder.receiverId");
				eqList.add(new ObjectId(recId));
				new BasicDBObject("$eq", eqList);
				orList.add(new BasicDBObject("$eq", eqList));
			}

			BasicDBObject param1 = new BasicDBObject("input", "$restrictedOrder");
			param1.append("as", "restrictedOrder");
			param1.append("cond", new BasicDBObject("$or", orList));

			BasicDBObject restrictedOrder = new BasicDBObject("restrictedOrder", new BasicDBObject("$filter", param1));

			Aggregation agg = Aggregation.newAggregation(
					Aggregation.match(criteria),
					new CustomAggregationOperation(
					        new BasicDBObject(
					            "$project",
					            restrictedOrder
					        )
						)
			);

			AggregationResults<Map> aggregate = template.aggregate(agg, "period", Map.class);
			return aggregate.getUniqueMappedResult();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List<Map> getSumOrder(String tab, List<Integer> type, String orderName, String periodId, String userId, String receiverId, String dealerId) {
		MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);

		Criteria criteria = Criteria.where("type").in(type).and("periodId").is(new ObjectId(periodId));

		if(!StringUtils.isBlank(userId)) {
			criteria.and("userId").is(new ObjectId(userId));
		}
		if(!StringUtils.isBlank(orderName)) {
			criteria.and("name").is(orderName);
		}

		String priceField, receiverIdFieldName;
		if(tab.equals("5") || tab.equals("51")) {
			priceField = "$todPrice";
			receiverIdFieldName = "todReceiverId";
		} else {
			priceField = "$price";
			receiverIdFieldName = "receiverId";
		}

		if(!StringUtils.isBlank(receiverId)) {
			criteria.and(receiverIdFieldName).is(new ObjectId(receiverId));
		}

		Aggregation agg = Aggregation.newAggregation(
				Aggregation.match(criteria),
				new CustomAggregationOperation(
				        new BasicDBObject(
				            "$group",
				            new BasicDBObject("_id", "$orderNumber")
				            .append("totalPrice", new BasicDBObject("$sum", priceField))
			                .append("count", new BasicDBObject("$sum", 1))
				        )
					),
				Aggregation.sort(Sort.Direction.DESC, "totalPrice")
		);

		AggregationResults<Map> aggregate = dealerTemp.aggregate(agg, "order", Map.class);
		List<Map> mappedResults = aggregate.getMappedResults();

		return mappedResults;
	}

/*	public Map getSumOrderTotal(String orderName, String periodId, String userId, String receiverId, List<Integer> typeLst, String dealerId) {
//		Integer[] spam = new Integer[] { 1 , 11 , 12 , 13 , 14 , 2, 21, 3, 31, 4 };
//		List<Integer> type = Arrays.asList(spam);

		MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);

		Criteria criteria = Criteria.where("type").in(typeLst).and("periodId").is(new ObjectId(periodId));

		if(!StringUtils.isBlank(userId)) {
			criteria.and("userId").is(new ObjectId(userId));
		}
		if(!StringUtils.isBlank(orderName)) {
			criteria.and("name").is(orderName);
		}
		if(!StringUtils.isBlank(receiverId)) {
			criteria.and("receiverId").is(new ObjectId(receiverId));
		}

		Aggregation agg = Aggregation.newAggregation(
				Aggregation.match(criteria),
				new CustomAggregationOperation(
						new BasicDBObject(
								"$group",
								new BasicDBObject("_id", "null")
								.append("totalPrice", new BasicDBObject("$sum", "$price"))
								.append("todPrice", new BasicDBObject("$sum", "$todPrice"))
								)
						)
				);

		AggregationResults<Map> aggregate = dealerTemp.aggregate(agg, "order", Map.class);
		Map result = aggregate.getUniqueMappedResult();

		return result;
	}*/

	public OrderName getOrderName(String userId, String prefix, String dealerId) {
		try {
			/*
			 *
			 *
			 db.getCollection('orderName').aggregate([
			{"$match": {"userId":ObjectId("57cd02376bdaf75408a3ae5f")}},
			{"$unwind" : "$names"},
			{"$match" : {"names.name" : {"$regex": "^ก.*"}}},
			{"$group" : {"_id": "$_id", "names" : {"$push": "$names.name"}}}
			])
			 *
			 */

			MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);
			OrderName orderName = dealerTemp.findOne(Query.query(Criteria.where("userId").is(new ObjectId(userId))), OrderName.class, "orderName");
			return orderName;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List getOrderNameByPeriod(String userId, String periodId, String dealerId) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);

			BasicDBObject dbObject = new BasicDBObject();
			dbObject.append("periodId", new ObjectId(periodId));

			if(!StringUtils.isBlank(userId)) {
				dbObject.append("userId", new ObjectId(userId));
			}

			return dealerTemp.getCollection("order").distinct("name", dbObject);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void saveResult(OrderCriteriaReq req) {
		try {
			Query query = Query.query(Criteria.where("_id").is(new ObjectId(req.getPeriodId())));
			Update update = new Update();
			update.set("result2", req.getResult2());
			update.set("result3", req.getResult3());

			template.updateFirst(query, update, "period");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void updateRestricted(OrderCriteriaReq req) {
		try {
			Query query = Query.query(Criteria.where("_id").is(new ObjectId(req.getPeriodId())));

			LOG.debug("Remove for new update");
			Update update = new Update();
			update.pull("restrictedOrder", new BasicDBObject("receiverId", new ObjectId(req.getReceiverId())));
			template.updateFirst(query, update, "period");

			LOG.debug("Update new value");
			boolean isUpdated = false;

			BasicDBObject restrictedOrderObje = new BasicDBObject();
			restrictedOrderObje.append("receiverId", new ObjectId(req.getReceiverId()));
			restrictedOrderObje.append("updatedDateTime", Calendar.getInstance().getTime());

			//--: No Price
			if(req.getNoPriceOrds().size() > 0) {
				isUpdated = true;
				restrictedOrderObje.append("noPrice", req.getNoPriceOrds());
			}
			//--: Half Price
			if(req.getHalfPriceOrds().size() > 0) {
				isUpdated = true;
				restrictedOrderObje.append("halfPrice", req.getHalfPriceOrds());
			}

			if(!isUpdated) return;

			update = new Update();
			update.push("restrictedOrder", restrictedOrderObje);

			template.updateFirst(query, update, "period");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private List<Map> getData1(String periodId, String userId, String receiverId, String dealerId) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);

			List<Integer> typeLst = getGroup("145678", true);
			Criteria criteria = Criteria.where("type").in(typeLst).and("periodId").is(new ObjectId(periodId));

			if(!StringUtils.isBlank(userId)) {
				criteria.and("userId").is(new ObjectId(userId));
			}
			if(!StringUtils.isBlank(receiverId)) {
				Criteria cr1 = Criteria.where("receiverId").is(new ObjectId(receiverId));
				Criteria cr2 = Criteria.where("todReceiverId").is(new ObjectId(receiverId));
				criteria.orOperator(cr1, cr2);
			}

			Query query = Query.query(criteria);
			query.fields()
			.include("orderNumber")
			.include("orderNumberAlias")
			.include("type")
			.include("probNum")
			.include("price")
			.include("isParent")
			.include("receiverId")
			.include("todReceiverId")
			.include("todPrice");
			query.with(new Sort(Sort.Direction.DESC, "price"));

			return dealerTemp.find(query, Map.class, "order");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public byte[] exportData(String periodId, String userId, Date periodDate, String receiverId, Receiver receiver, String dealerId) throws Exception {
		try {
			List<String> ordFormatedLst3 = new ArrayList<>();
			List<String> ordFormatedLst2Bon = new ArrayList<>();
			List<String> ordFormatedLst2Lang = new ArrayList<>();
			List<String> ordFormatedLstLoy = new ArrayList<>();
			String jasperFile = basePath + "/jasper/order.jasper";
			String priceStr, todPriceStr;
			String ordFormated = "", orderNumber = "";
			boolean isParent;
			int type;
			Map order;

			//---: getData
			List<Map> orders = getData1(periodId, userId, receiverId, dealerId);
			double bon3Chk = 0, loyChk = 0, bon2Chk = 0, lang2Chk = 0,
				   todChk = 0, pare4 = 0, pare5 = 0, runBon = 0, runLang = 0;
			double price, todPrice;
			int probNum;

			for (int i = 0; i < orders.size(); i++) {
				order = orders.get(i);
				type = (int)order.get("type");
				isParent = (boolean)order.get("isParent");
				orderNumber = order.get("orderNumber").toString();

				if(type == OrderTypeConstant.TYPE1.getId() ||
					type == OrderTypeConstant.TYPE11.getId() ||
					type == OrderTypeConstant.TYPE12.getId() ||
					type == OrderTypeConstant.TYPE13.getId() ||
					type == OrderTypeConstant.TYPE14.getId()) {

					if(type == OrderTypeConstant.TYPE11.getId() && !isParent) continue;
					if(type == OrderTypeConstant.TYPE12.getId() && !isParent) continue;

					if(type == OrderTypeConstant.TYPE1.getId()) {
						price = (double)order.get("price");
						bon3Chk += price;

						priceStr = String.format("%,.0f", price);
						ordFormated = orderNumber + " = " + priceStr + "\n";
					} else if(type == OrderTypeConstant.TYPE11.getId() ||
						type == OrderTypeConstant.TYPE12.getId()) {

						if(type == OrderTypeConstant.TYPE12.getId()) {
							orderNumber = order.get("orderNumberAlias").toString();
						}
						price = (double)order.get("price");
						probNum = (int)order.get("probNum");
						bon3Chk += (price * probNum);

						priceStr = String.format("%,.0f", price);
						ordFormated = orderNumber + " = " + priceStr + "x" + probNum + "\n";
					} else if(type == OrderTypeConstant.TYPE13.getId()) {
						if(StringUtils.isNotBlank(receiverId)) {
							if(receiverId.equals(order.get("receiverId").toString())) {
								price = (double)order.get("price");
								priceStr = String.format("%,.0f", price);
								bon3Chk += price;
								if(receiverId.equals(order.get("todReceiverId").toString())) {
									todPrice = (double)order.get("todPrice");
									todChk += todPrice;

									todPriceStr = String.format("%,.0f", todPrice);
									ordFormated = orderNumber + " = " + priceStr + "x" + todPriceStr + "\n";
								} else {
									ordFormated = orderNumber + " = " + priceStr + "\n";
								}
							} else {
								todPrice = (double)order.get("todPrice");
								todChk += todPrice;

								todPriceStr = String.format("%,.0f", todPrice);
								ordFormated = orderNumber + " = " + todPriceStr + " เฉพาะโต๊ด\n";
							}
						}
					} else if(type == OrderTypeConstant.TYPE14.getId()) {
						price = (double)order.get("price");
						todPrice = (double)order.get("todPrice");
						probNum = (int)order.get("probNum");
						bon3Chk += (price * probNum);
						todChk += todPrice;

						priceStr = String.format("%,.0f", price);
						todPriceStr = String.format("%,.0f", todPrice);
						ordFormated = orderNumber + " = " + priceStr + "x" + probNum + "x" + todPriceStr + "\n";
					} else {
						continue;
					}
					ordFormatedLst3.add(ordFormated);
				} else if(type == OrderTypeConstant.TYPE4.getId() ||
						type == OrderTypeConstant.TYPE41.getId() ||
						type == OrderTypeConstant.TYPE42.getId() ||
						type == OrderTypeConstant.TYPE43.getId() ||
						type == OrderTypeConstant.TYPE44.getId()) {
					price = (double)order.get("price");

					String label = "";
					if(type == OrderTypeConstant.TYPE4.getId()) {
						label = "ลอย";
						loyChk += price;
					} else if(type == OrderTypeConstant.TYPE41.getId()) {
						label = "แพ";
						pare4 += price;
					} else if(type == OrderTypeConstant.TYPE42.getId()) {
						label = "แพ";
						pare5 += price;
					} else if(type == OrderTypeConstant.TYPE43.getId()) {
						label = "วิ่งบน";
						runBon += price;
					} else if(type == OrderTypeConstant.TYPE44.getId()) {
						label = "วิ่งล่าง";
						runLang += price;
					}

					priceStr = String.format("%,.0f", price);
					ordFormated = label + " " + orderNumber + " = " + priceStr + "\n";
					ordFormatedLstLoy.add(ordFormated);
				}
			}

			// 2 ตัวบน
			List<Integer> typeLst = Arrays.asList(new Integer[] { 2, 21});

			//---: getData
			orders = getData2(periodId, userId,  typeLst, receiverId, dealerId);

			for (int i = 0; i < orders.size(); i++) {
				order = orders.get(i);
				orderNumber = order.get("_id").toString();
				price = (double)order.get("price");
				bon2Chk += price;

				priceStr = String.format("%,.0f", price);
				ordFormated = orderNumber + " = " + priceStr + "\n";
				ordFormatedLst2Bon.add(ordFormated);
			}

			// 2 ตัวล่าง
			typeLst = Arrays.asList(new Integer[] { 3, 31});

			//---: getData
			orders = getData2(periodId, userId,  typeLst, receiverId, dealerId);

			for (int i = 0; i < orders.size(); i++) {
				order = orders.get(i);
				orderNumber = order.get("_id").toString();
				price = (double)order.get("price");
				lang2Chk += price;

				priceStr = String.format("%,.0f", price);
				ordFormated = orderNumber + " = " + priceStr + "\n";
				ordFormatedLst2Lang.add(ordFormated);
			}

			//--------------------------------------------------

			//--- Fields
			String period = String.format(new Locale("th", "TH"), "%1$td %1$tb %1$tY", periodDate);
			Map<Object, Object> hashMap = new HashMap<>();
			hashMap.put("period", period);
			List<Map> data = null;
			List<String> group = new ArrayList<>();
			group.add(receiver.getSenderName() + " 3 ตัวตรง");
			group.add(receiver.getSenderName() + " 2 ตัวบน");
			group.add(receiver.getSenderName() + " 2 ตัวล่าง");
			byte[] pdfByte = null, pdfByte2 = null;
			List<String> formatedOrder;

			for (int i = 0; i < group.size(); i++) {
				hashMap.put("title", group.get(i));
				data = new ArrayList<>();

				if(i == 0) {
					//-- Included Loy to 3;
					ordFormatedLst3.addAll(ordFormatedLstLoy);
					if(ordFormatedLst3.size() == 0) continue;

					formatedOrder = formatedOrder(ordFormatedLst3);

					hashMap.put("order1", formatedOrder.get(0));
					hashMap.put("order2", formatedOrder.get(1));
					data.add(hashMap);

					//--
					pdfByte = new JasperReportEngine().toPdf(jasperFile, data, null);
				} else if(i == 1) {
					if(ordFormatedLst2Bon.size() == 0) continue;

					formatedOrder = formatedOrder(ordFormatedLst2Bon);
					hashMap.put("order1", formatedOrder.get(0));
					hashMap.put("order2", formatedOrder.get(1));
					data.add(hashMap);

					if(pdfByte == null) {
						pdfByte = new JasperReportEngine().toPdf(jasperFile, data, null);
					} else {
						pdfByte2 = new JasperReportEngine().toPdf(jasperFile, data, null);
						pdfByte = mergePdf(pdfByte, pdfByte2);
					}
				} else {
					if(ordFormatedLst2Lang.size() == 0) continue;

					formatedOrder = formatedOrder(ordFormatedLst2Lang);
					hashMap.put("order1", formatedOrder.get(0));
					hashMap.put("order2", formatedOrder.get(1));
					data.add(hashMap);

					if(pdfByte == null) {
						pdfByte = new JasperReportEngine().toPdf(jasperFile, data, null);
					} else {
						pdfByte2 = new JasperReportEngine().toPdf(jasperFile, data, null);
						pdfByte = mergePdf(pdfByte, pdfByte2);
					}
				}
			}

			//---------: Checking :------
			String bon3ChkStr, bon2ChkStr, lang2ChkStr, loyChkStr, todChkStr, pare4ChkStr, pare5ChkStr, runBonChkStr, runLangChkStr;

			bon3ChkStr = String.format("%,.0f", bon3Chk);
			bon2ChkStr = String.format("%,.0f", bon2Chk);
			lang2ChkStr = String.format("%,.0f", lang2Chk);
			todChkStr = String.format("%,.0f", todChk);
			loyChkStr = String.format("%,.0f", loyChk);
			pare4ChkStr = String.format("%,.0f", pare4);
			pare5ChkStr = String.format("%,.0f", pare5);
			runBonChkStr = String.format("%,.0f", runBon);
			runLangChkStr = String.format("%,.0f", runLang);

			LOG.info("3 ตัวบน: " + bon3ChkStr + ", 2 ตัวบน: " + bon2ChkStr + ", 2 ตัวล่าง: " + lang2ChkStr + ", โต๊ด: " + todChkStr +
					", ลอย: " + loyChkStr + ", แพ 4: " + pare4ChkStr + ", แพ 5: " + pare5ChkStr +
					", วิ่งบน: " + runBonChkStr + ", วิ่งล่าง: " + runLangChkStr);
			//---------: Checking :------

			return zipFile(pdfByte, period);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

/*	public OrderCriteriaResp checkResult(String periodId, Boolean isAllReceiver, String dealerId) {
		try {
			OrderCriteriaResp resp = new OrderCriteriaResp();

			Query query = Query.query(Criteria.where("_id").is(new ObjectId(periodId)));
			Map period = template.findOne(query, Map.class, "period");
			String result2 = period.get("result2") == null ? null : period.get("result2").toString();
			String result3 = period.get("result3") == null ? null : period.get("result3").toString();

			if(StringUtils.isBlank(result2) || StringUtils.isBlank(result3)) return resp;

			Map<String, Map<String, List<Map>>> multiRc = new HashMap<>();
			Map<String, List<Map>> chkResultMap;

			if(isAllReceiver != null && isAllReceiver) {
				List<Receiver> receiverList = receiverService.getReceiverList(true, dealerId);

				for (Receiver rc : receiverList) {
					chkResultMap = checkResult(periodId, result3, result2, rc.getId(), dealerId);
					multiRc.put(rc.getId(), chkResultMap);
				}
			} else {
				chkResultMap = checkResult(periodId, result3, result2, null, dealerId);
				multiRc.put("total", chkResultMap);
			}

			resp.setChkResultMap(multiRc);

			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}*/

	public OrderCriteriaResp checkResult(String periodId, String dealerId) {
		try {
			OrderCriteriaResp resp = new OrderCriteriaResp();

			Query query = Query.query(Criteria.where("_id").is(new ObjectId(periodId)));
			Map period = template.findOne(query, Map.class, "period");
			String result2 = period.get("result2") == null ? null : period.get("result2").toString();
			String result3 = period.get("result3") == null ? null : period.get("result3").toString();

			if(StringUtils.isBlank(result2) || StringUtils.isBlank(result3)) return resp;

			List<Map> chkResultList = new ArrayList<>();
			Map<String, List<Map>> chkResultMap;
			List<Receiver> receiverList = receiverService.getReceiverList(true, dealerId);

			for (Receiver rc : receiverList) {
				chkResultMap = checkResult(rc, periodId, result3, result2, rc.getId(), dealerId);
				if(((List)chkResultMap.get("result")).size() > 0) {
					chkResultList.add(chkResultMap);
				}
			}

			resp.setChkResultList(chkResultList);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	/**
	 * Get data on time line that input to system.
	 */
	public Map<String, Object> getDataOnTL(String periodId, String userId, String orderName, List<Integer> typeLst, String receiverId, Sort sort, String dealerId) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);

			Criteria criteria = Criteria.where("periodId").is(new ObjectId(periodId))
			.and("type").in(typeLst).and("isParent").is(true);

			if(!StringUtils.isBlank(userId)) {
				criteria.and("userId").is(new ObjectId(userId));
			}
			if(!StringUtils.isBlank(orderName)) {
				criteria.and("name").is(orderName);
			}
			if(!StringUtils.isBlank(receiverId)) {
				Criteria cr1 = Criteria.where("receiverId").is(new ObjectId(receiverId));
				Criteria cr2 = Criteria.where("todReceiverId").is(new ObjectId(receiverId));
				criteria.orOperator(cr1, cr2);
			}

			Query query = Query.query(criteria);
			if(sort != null) query.with(sort);

			List<Map> orderLst = dealerTemp.find(query, Map.class, "order");
			String orderNumber, symbol = "", note = "", recId = "", todReceiverId = "";
			double price, todPrice, sumOrderTotal = 0;
			int type, probNum;
			String priceStr, todPriceStr;

			for (Map order : orderLst) {
				orderNumber = order.get("orderNumber").toString();
				type = (int)order.get("type");
				probNum = (int)order.get("probNum");
				price = (double)order.get("price");
				priceStr = String.format("%,.0f", price);

				if(type == 2) {
					note = "บน";
					sumOrderTotal += price;
				} else if(type == 3) {
					note = "ล่าง";
					sumOrderTotal += price;
				} else if(type == 21) {
					symbol = " x " + probNum;
					note = "บน";
					sumOrderTotal += price * probNum;
				} else if(type == 31) {
					symbol = " x " + probNum;
					note = "ล่าง";
					sumOrderTotal += price * probNum;
				} else if(type == 1) {
					sumOrderTotal += price;
				} else if(type == 11) {
					symbol = " x " + probNum;
					sumOrderTotal += price * probNum;
				} else if(type == 12) {
					orderNumber = order.get("orderNumberAlias").toString();
					symbol = " x " + probNum;
					sumOrderTotal += price * probNum;
				} else if(type == 13) {
					todPrice = (double)order.get("todPrice");
					todPriceStr = String.format("%,.0f", todPrice);
					symbol = " x " + todPriceStr;
					sumOrderTotal += price;
					sumOrderTotal += todPrice;

					if(StringUtils.isNotBlank(receiverId)) {
						recId = order.get("receiverId").toString();
						if(receiverId.equals(recId)) {
							todReceiverId = order.get("todReceiverId").toString();
							if(!receiverId.equals(todReceiverId)) {
								symbol = "";
								note = "แยกโต๊ด";
								sumOrderTotal -= todPrice;
							}
						} else {
							sumOrderTotal -= price;
							symbol = "";
							priceStr = todPriceStr;
							note = "โต๊ด";
							order.put("type", 131);
						}
					}
				} else if(type == 14) {
					todPrice = (double)order.get("todPrice");
					todPriceStr = String.format("%,.0f", todPrice);
					symbol = " x " + probNum + " x " + todPriceStr;
					sumOrderTotal += price * probNum;
				} else if(type == 4) {
					note = "ลอย";
					sumOrderTotal += price;
				} else if(type == 41) {
					note = "แพ 4";
					sumOrderTotal += price;
				} else if(type == 42) {
					note = "แพ 5";
					sumOrderTotal += price;
				} else if(type == 43) {
					note = "วิ่งบน";
					sumOrderTotal += price;
				} else if(type == 44) {
					note = "วิ่งล่าง";
					sumOrderTotal += price;
				} else {
					LOG.debug("type: " + type);
				}

				order.put("symBol", orderNumber + " = " + priceStr + symbol);
				order.put("note", note);

				symbol = "";
				note = "";
			}

			Map<String, Object> result = new HashMap<>();
			result.put("orderLst", orderLst);
			result.put("sumOrderTotal", sumOrderTotal);

			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void moveToReceiver(OrderCriteriaReq req) throws Exception {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());

			LOG.debug("Start prepareRestrictedNumber");
			List<String> ids = new ArrayList<>();
			ids.add(req.getReceiverId());

			OrderCriteriaReq reqRest = new OrderCriteriaReq();
			reqRest.setPeriodId(req.getPeriodId());
			reqRest.setReceiverIds(ids);

			Object restrictedOrderObj = prepareRestrictedNumber(getRestrictedOrder(reqRest), true).get(req.getReceiverId());
			Map noPrice = null;
			if(restrictedOrderObj != null) {
				Map restrictedOrderMap = (Map)restrictedOrderObj;
				noPrice = (Map)restrictedOrderMap.get("noPrice");
			}
			LOG.debug("End prepareRestrictedNumber");

			Criteria criteria1 = Criteria.where("parentId").is(new ObjectId(req.getOrderId()));
			Criteria criteria2 = Criteria.where("_id").is(new ObjectId(req.getOrderId()));

			Query query = Query.query(new Criteria().orOperator(criteria1, criteria2));
			query.fields()
			.include("id")
			.include("type")
			.include("orderNumber")
			.include("receiverId")
			.include("todReceiverId");

			List<Order> orders = dealerTemp.find(query, Order.class);

			for (Order order : orders) {
				if(req.getType().intValue() == 131) {
					if(order.getTodReceiverId().toString().equals(req.getReceiverId())) throw new Exception("Moving to the same receiver.");
					break;
				} else {
					if(order.getReceiverId().toString().equals(req.getReceiverId())) throw new Exception("Moving to the same receiver.");
				}

				try {
					LOG.debug("Start call restrictedCheck noPrice");
					restrictedCheck(order.getType(), noPrice, order.getOrderNumber());
				} catch (CustomerException e) {
					LOG.warn(order.getOrderNumber() + " is in restriction number.");
					throw e;
				}
			}

			Update update = new Update();
			if(req.getType().intValue() == 131) {
				LOG.info("Move only tod.");
				update.set("todReceiverId", new ObjectId(req.getReceiverId()));
				dealerTemp.updateMulti(query, update, Order.class);
			} else if(req.getType().intValue() == 13) {
				LOG.info("Move with tod.");
				update.set("receiverId", new ObjectId(req.getReceiverId()));
				update.set("todReceiverId", new ObjectId(req.getReceiverId()));
				dealerTemp.updateMulti(query, update, Order.class);
			} else {
				LOG.info("Move normal.");
				update.set("receiverId", new ObjectId(req.getReceiverId()));
				dealerTemp.updateMulti(query, update, Order.class);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public int moveToReceiverWithCond(OrderCriteriaReq req, List<Integer> types) throws Exception {
		try {
			LOG.debug("Get Move-from data");
			List<Map> orderDataMainList = null;
			if(req.getOperator().equals("3")) {
				orderDataMainList = (List<Map>)getDataOnTL(req.getPeriodId(), req.getUserId(), null, types, req.getMoveFromId(), null, req.getDealerId()).get("orderLst");
			} else {
				orderDataMainList = (List<Map>)getDataOnTL(req.getPeriodId(), req.getUserId(), null, types, req.getMoveFromId(), new Sort(Sort.Direction.DESC, "price"), req.getDealerId()).get("orderLst");
			}

			if(orderDataMainList == null || orderDataMainList.size() == 0) return 0;

			boolean isMoveTodOnly = true;
			Map noPrice = null;
			if(!req.getTab().equals("51")) {
				LOG.debug("Start prepareRestrictedNumber");
				isMoveTodOnly = false;
				List<String> recIds = new ArrayList<>();
				recIds.add(req.getMoveToId());

				OrderCriteriaReq reqRest = new OrderCriteriaReq();
				reqRest.setPeriodId(req.getPeriodId());
				reqRest.setReceiverIds(recIds);

				Object restrictedOrderObj = prepareRestrictedNumber(getRestrictedOrder(reqRest), true).get(req.getMoveToId());
				if(restrictedOrderObj != null) {
					Map restrictedOrderMap = (Map)restrictedOrderObj;
					noPrice = (Map)restrictedOrderMap.get("noPrice");
				}
				LOG.debug("End prepareRestrictedNumber");
			}

			List<ObjectId> ids;
			if(req.getOperator().equals("1")) { // Less than or equal.

				LOG.debug("Get Move-to data");
				List<Map> sumOrderLst = getSumOrder(
					req.getTab(), types, null, req.getPeriodId(), req.getUserId(), req.getMoveToId(), req.getDealerId()
				);

				Map<String, Double> sumOrderMoveTo = new HashMap<>();
				for (Map sumOrder : sumOrderLst) {
					sumOrderMoveTo.put(sumOrder.get("_id").toString(), Double.valueOf(sumOrder.get("totalPrice").toString()));
				}

				LOG.debug("call moveLte");
				ids = moveLte(req, orderDataMainList, types, sumOrderMoveTo);
			} else if(req.getOperator().equals("2")) { // Greater than or equal.
				LOG.debug("call moveGt");
				ids = moveAllOrGt(req, orderDataMainList, types, noPrice, false, isMoveTodOnly);
			} else { // All
				ids = moveAllOrGt(req, orderDataMainList, types, noPrice, true , isMoveTodOnly);
			}

			if(ids.size() == 0) return 0;

			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());

			LOG.debug("Changing receiver.");
			Update update = new Update();
			if(isMoveTodOnly) {
				update.set("todReceiverId", new ObjectId(req.getMoveToId()));
			} else {
				update.set("receiverId", new ObjectId(req.getMoveToId()));
			}

			Query query = Query.query(new Criteria().orOperator(Criteria.where("_id").in(ids), Criteria.where("parentId").in(ids)));
			dealerTemp.updateMulti(query, update, "order");

			//--: Update receicerId for TOD.
			if(req.getTab().equals("1")) {
				if(req.getIsIncludeTod()) {
					LOG.info("Update with tod");
					update = new Update();
					update.set("todReceiverId", new ObjectId(req.getMoveToId()));

					List<Integer> typeLst = new ArrayList<>();
					typeLst.add(13);
					typeLst.add(131);
					Criteria criteria = Criteria.where("type").in(typeLst).orOperator(Criteria.where("_id").in(ids), Criteria.where("parentId").in(ids));
					query = Query.query(criteria);
					dealerTemp.updateMulti(query, update, "order");
				} else {
					LOG.info("Update without tod");
				}
			}

			return ids.size();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	/**
	 *
	 * @param req
	 * @param orderDataMainList
	 * @param types
	 * @param sumOrderMoveTo
	 * @return
	 * @throws Exception
	 * Greater than
	 */
	private List<ObjectId> moveAllOrGt(OrderCriteriaReq req, List<Map> orderDataMainList, List<Integer> types, Map noPrice, boolean isAll, boolean isMoveTodOnly) throws Exception {
		try {
			LOG.debug("Move on greater than.");
			Map<String, Double> sumOrderMoveTo = new HashMap<>();
			List<ObjectId> ids = new ArrayList<>();
			List<String> orderNumProb;
			Double moveToPrice;
			String orderNumber;
			int probNum, type;
			double price, addedPrice;
			double priceCond = req.getPrice().doubleValue();
			boolean isMove;

			LOG.debug("Start moving the data");
			for (Map data : orderDataMainList) {
					type = 0;
					orderNumProb = null;
					isMove = false;

					price = isMoveTodOnly ? (double)data.get("todPrice") : (double)data.get("price");
					orderNumber = data.get("orderNumber").toString();
					type = (int)data.get("type");

					probNum = (int)data.get("probNum");
					if(probNum > 1) {
						if(type != 13 && type != 131) {
							orderNumProb = getOrderNumProb(orderNumber);
						}
					}

					if(orderNumProb == null) {
						orderNumProb = new ArrayList<>();
						orderNumProb.add(orderNumber);
					}

					for (String ordNum : orderNumProb) {
						try {
							LOG.debug("Start call restrictedCheck noPrice");
							restrictedCheck(type, noPrice, orderNumber);
							isMove = true;
						} catch (CustomerException e) {
							isMove = false;
							LOG.warn(orderNumber + " is in restriction number.");
							break;
						}

						if(!isAll) {
							isMove = false;
							if(sumOrderMoveTo.size() > 0) {
								moveToPrice = sumOrderMoveTo.get(ordNum);
								if(moveToPrice != null) {
									addedPrice = moveToPrice.doubleValue() + price;
									if(addedPrice > priceCond) {
										LOG.info(orderNumber + " will be moved " + priceCond + " {" + price + "}");
										isMove = true;
										break;
									} else {
										sumOrderMoveTo.put(ordNum, moveToPrice.doubleValue() + price);
									}
								} else {
									if(price > priceCond) {
										LOG.info(orderNumber + " will be moved " + priceCond + " {" + price + "}");
										isMove = true;
										break;
									} else {
										sumOrderMoveTo.put(ordNum, price);
									}
								}
							} else {
								if(price > priceCond) {
									LOG.info(orderNumber + " will be moved " + priceCond + " {" + price + "}");
									isMove = true;
									break;
								} else {
									sumOrderMoveTo.put(ordNum, price);
								}
							}
						} // isAll
					}

					if(!isMove) continue;

					// Get ID to move receiver.
					ids.add((ObjectId)data.get("_id"));
			}
			return ids;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	/**
	 *
	 * @param req
	 * @param orderDataMainList
	 * @param types
	 * @param sumOrderMoveTo
	 * @return
	 * @throws Exception
	 * Less than or equal
	 */
	private List<ObjectId> moveLte(OrderCriteriaReq req, List<Map> orderDataMainList, List<Integer> types, Map<String, Double> sumOrderMoveTo) throws Exception {
		try {
			LOG.debug("Move on less than or equal.");
			Map<String, Double> sumOrderMoveToDummy;
			List<ObjectId> ids = new ArrayList<>();
			List<String> orderNumProb;
			Double moveToPrice;
			String orderNumber;
			int probNum, type;
			double price, addedPrice;
			double priceCond = req.getPrice().doubleValue();

			LOG.debug("Start moving the data");
			outer: for (Map data : orderDataMainList) {
					type = 0;
					orderNumProb = null;
					sumOrderMoveToDummy = new HashMap<>();

					price = (double)data.get("price");
					orderNumber = data.get("orderNumber").toString();

					// Condition Check 1st
					if(price > priceCond) {
						LOG.info(orderNumber + " is OVER " + priceCond + " {" + price + "}");
						continue;
					}

					// Condition Check 2nd
					probNum = (int)data.get("probNum");
					if(probNum > 1) {
						type = (int)data.get("type");

						if(type != 13) {
							orderNumProb = getOrderNumProb(orderNumber);
						}
					}

					if(orderNumProb == null) {
						orderNumProb = new ArrayList<>();
						orderNumProb.add(orderNumber);
					}

					// Condition Check 3th
					for (String ordNum : orderNumProb) {
						if(sumOrderMoveTo.size() > 0) {
							moveToPrice = sumOrderMoveTo.get(ordNum);
							if(moveToPrice != null) {
								addedPrice = moveToPrice.doubleValue() + price;
								if(addedPrice > priceCond) {
									LOG.info(ordNum + " is OVER " + priceCond + " when add to exiting price. {" + addedPrice + "}");
									continue outer; // Go to outer for loop.
								} else {
									sumOrderMoveToDummy.put(ordNum, moveToPrice.doubleValue() + price);
								}
							} else {
								sumOrderMoveToDummy.put(ordNum, price);
							}
						} else {
							sumOrderMoveToDummy.put(ordNum, price);
						}
					}

					// Update to sumOrderMoveTo object.
					for (Map.Entry<String, Double> entry : sumOrderMoveToDummy.entrySet()) {
						sumOrderMoveTo.put(entry.getKey(), entry.getValue());
					}

					// Get ID to move receiver.
					ids.add((ObjectId)data.get("_id"));
			}
			return ids;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List<Integer> getGroup(String group, boolean isCheck) {
		List<Integer> typeLst = new ArrayList<>();

		if(isCheck) {
			if(group.contains("1")) {
				typeLst.add(1);
				typeLst.add(11);
				typeLst.add(12);
				typeLst.add(13);
				typeLst.add(14);
			}
			if(group.contains("2")) {
				typeLst.add(2);
				typeLst.add(21);
			}
			if(group.contains("3")) {
				typeLst.add(3);
				typeLst.add(31);
			}
			if(group.contains("4")) {
				typeLst.add(4);
			}
			if(group.contains("5")) {
				typeLst.add(41);
			}
			if(group.contains("6")) {
				typeLst.add(42);
			}
			if(group.contains("7")) {
				typeLst.add(43);
			}
			if(group.contains("8")) {
				typeLst.add(44);
			}
		} else {
			if(group.equals("1")) {
				typeLst.add(1);
				typeLst.add(11);
				typeLst.add(12);
				typeLst.add(13);
				typeLst.add(14);
			} else if(group.equals("2")) {
				typeLst.add(2);
				typeLst.add(21);
			} else if(group.equals("3")) {
				typeLst.add(3);
				typeLst.add(31);
			} else if(group.equals("4")) {
				typeLst.add(4);
			} else if(group.equals("41")) {
				typeLst.add(41);
			} else if(group.equals("42")) {
				typeLst.add(42);
			} else if(group.equals("43")) {
				typeLst.add(43);
			} else if(group.equals("44")) {
				typeLst.add(44);
			} else if(group.equals("5")) {
				typeLst.add(13);
				typeLst.add(14);
			} else if(group.equals("51")) {
				typeLst.add(13);
				typeLst.add(131);
			}
		}

		return typeLst;
	}

	public Map prepareRestrictedNumber(Map source, boolean isTranslated) throws Exception {
		try {
			Map restrictedOrderResult = new HashMap();
			Object restrictedOrderObj = source.get("restrictedOrder");

			if(restrictedOrderObj != null) {
				List<Map> restrictedOrderLst = (List<Map>)restrictedOrderObj;
				Map restrictedOrderMap;

				for (Map map : restrictedOrderLst) {
					restrictedOrderMap = new HashMap();

					if(isTranslated) {
						restrictedOrderMap.put("noPrice", restrictedTranslate(map.get("noPrice")));
						restrictedOrderMap.put("halfPrice", restrictedTranslate(map.get("halfPrice")));
					} else {
						restrictedOrderMap.put("noPrice", map.get("noPrice"));
						restrictedOrderMap.put("halfPrice", map.get("halfPrice"));
					}
					restrictedOrderResult.put(map.get("receiverId").toString(), restrictedOrderMap);
				}
			}
			return restrictedOrderResult;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	//-----------------------: Private :------------------------------
	private Map<String, List<String>> restrictedTranslate(Object source) throws Exception {
		try {
			Map<String, List<String>> resultMap = new HashMap<>();
			if(source == null) return resultMap;

			resultMap.put("bon3", new ArrayList<String>());
			resultMap.put("bon2", new ArrayList<String>());
			resultMap.put("lang2", new ArrayList<String>());
			resultMap.put("all", new ArrayList<String>());

			List<Map> sourceMap = (List<Map>)source;
			String orderNumber;
			String type;
			for (Map map : sourceMap) {
				type = "";
				orderNumber = map.get("orderNumber").toString();

				if(orderNumber.contains("*")) {
					type += "1";
					orderNumber = orderNumber.replace("*", "");
				}
				if(orderNumber.contains(">")) {
					type += "2";
					orderNumber = orderNumber.replace(">", "");
				} else if(orderNumber.contains("<")) {
					type += "3";
					orderNumber = orderNumber.replace("<", "");
				}

				List<String> orderNumProb;
				if(type.contains("1")) {
					orderNumProb = getOrderNumProb(orderNumber);
				} else {
					orderNumProb = new ArrayList<>();
					orderNumProb.add(orderNumber);
				}
				if(orderNumber.length() == 3) {
					resultMap.get("bon3").addAll(orderNumProb);
				} else {
					if(type.contains("2")) {
						resultMap.get("bon2").addAll(orderNumProb);
					} else if(type.contains("3")) {
						resultMap.get("lang2").addAll(orderNumProb);
					} else {
						resultMap.get("all").addAll(orderNumProb);
					}
				}
			}

			return resultMap;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private List<String> getOrderNumProbOver3(String src) throws Exception {
		List<String> orderNumLst = new ArrayList<>();
		int orderSet[][];
		if(src.length() == 4) {
			orderSet = new int[][] {{0,1,2},{0,1,3},{0,2,3},{1,2,3}};
		} else if(src.length() == 5) {
			orderSet = new int[][] {{0,1,2},{0,1,3},{0,1,4},{0,2,3},{0,2,4},{0,3,4},{1,2,3},{1,2,4},{1,3,4},{2,3,4}};
		} else {
			throw new Exception("Number of digit is not support.");
		}

		String result = "";

		for (int i = 0; i < orderSet.length; i++) {
			for (int j = 0; j < orderSet[i].length; j++) {
				result += src.charAt(orderSet[i][j]);
			}
			if(orderNumLst.contains(result)) {
				result = "";
				continue;
			}
			orderNumLst.addAll(getOrderNumProb(result));
			result = "";
		}
		return orderNumLst;
	}

	private List<String> getOrderNumProb(String src) throws Exception {
		List<String> orderNumLst = new ArrayList<>();
		String result = "";
		int orderSet[][];

		if(src.length() == 2) {
			orderSet = new int[][] {{0,1}, {1,0}};
		} else if(src.length() == 3) {
			orderSet = new int[][] {{0,1,2}, {1,2,0}, {2,0,1}, {2,1,0}, {1,0,2}, {0,2,1}};
		} else {
			throw new Exception("Number of digit is not support.");
		}

		orderNumLst.add(src);

		outer: for (int i = 1; i < orderSet.length; i++) {
			for (int j = 0; j < orderSet[i].length; j++) {
				result += src.charAt(orderSet[i][j]);
			}
			if(orderNumLst.contains(result)) {
				break outer;
			}

			orderNumLst.add(result);
			result = "";
		}
		return orderNumLst;
	}

	private List<Order> prepareDbObj(
			List<String> orderNumProb, String name, Integer parentType,
			Integer childType, Double parentPrice, Double childPrice, String userId,
			String periodId, String orderNumberAlias, String receiverId, Map noPrice, Map halfPrice) throws Exception {

		Date now = Calendar.getInstance().getTime();
		List<Order> objLst = new ArrayList<>();
		ObjectId id = null;
		Order order;
		Double childPriceDummy = childPrice;

		if(parentType.intValue() == 14) {
			childType = 11;
			childPrice = parentPrice;
		}

		for (int i = 0; i < orderNumProb.size(); i++) {
			order = new Order();
			order.setReceiverId(new ObjectId(receiverId));
			order.setCreatedDateTime(now);
			order.setName(name);
			order.setOrderNumber(orderNumProb.get(i));
			order.setUserId(new ObjectId(userId));
			order.setPeriodId(new ObjectId(periodId));

			if(i == 0) {
				if(orderNumProb.size() > 1) {
					id = ObjectId.get();
					order.setId(id.toString());
				}
				order.setOrderNumberAlias(orderNumberAlias);
				order.setIsParent(true);
				order.setType(parentType);
				order.setPrice(parentPrice);
				order.setProbNum(orderNumProb.size());

				if(parentType.intValue() == OrderTypeConstant.TYPE13.getId() ||
						parentType.intValue() == OrderTypeConstant.TYPE14.getId()) {
					order.setTodPrice(childPriceDummy);
					order.setTodReceiverId(new ObjectId(receiverId));
				}
			} else {
				order.setParentId(id);
				order.setIsParent(false);
				order.setType(childType);
				if(childType.intValue() == OrderTypeConstant.TYPE131.getId()) {
					order.setTodPrice(childPrice);
					order.setTodReceiverId(new ObjectId(receiverId));
				} else {
					order.setPrice(childPrice);
				}
			}

			//--: Check Restricted
			LOG.debug("Start call restrictedCheck noPrice");
			restrictedCheck(order.getType(), noPrice, order.getOrderNumber());
			try {
				LOG.debug("Start call restrictedCheck halfPrice");
				restrictedCheck(order.getType(), halfPrice, order.getOrderNumber());
			} catch (CustomerException e) {
				order.setIsHalfPrice(true);  // Set half price to number that is in the list.
				if(objLst.size() > 0) {
					objLst.get(0).setIsHalfPrice(true);  // Set half price to parent as well.
				}
				LOG.warn(order.getOrderNumber() + " is in HALF price restriction.");
			}
			LOG.debug("End call restrictedCheck");
			//--: Check Restricted

			objLst.add(order);
		}

		if(parentType.intValue() == 14) {
			LOG.debug("IsTod");
			orderNumProb.remove(0);
			childType = 131;
			childPrice = childPriceDummy;

			for (int i = 0; i < orderNumProb.size(); i++) {
				order = new Order();
				order.setReceiverId(new ObjectId(receiverId));
				order.setTodReceiverId(new ObjectId(receiverId));
				order.setCreatedDateTime(now);
				order.setName(name);
				order.setOrderNumber(orderNumProb.get(i));
				order.setType(childType);
//				order.setPrice(childPrice);
				order.setTodPrice(childPrice);
				order.setUserId(new ObjectId(userId));
				order.setPeriodId(new ObjectId(periodId));
				order.setParentId(id);
				order.setIsParent(false);

				objLst.add(order);
			}
		}

		return objLst;
	}

	private void restrictedCheck(int type, Map mapData, String orderNumber) throws Exception {
		try {
			if(mapData == null) return;
			List<String> bon3 = (List<String>)mapData.get("bon3");
			List<String> bon2 = (List<String>)mapData.get("bon2");
			List<String> lang2 = (List<String>)mapData.get("lang2");
			List<String> all = (List<String>)mapData.get("all");

			if(type == OrderTypeConstant.TYPE1.getId() ||
					type == OrderTypeConstant.TYPE11.getId() ||
					type == OrderTypeConstant.TYPE12.getId() ||
					type == OrderTypeConstant.TYPE13.getId() ||
					type == OrderTypeConstant.TYPE14.getId()) {

				if(bon3 != null) {
					if(bon3.contains(orderNumber)) {
						throw new CustomerException(1, orderNumber + " in restricted number {3 ตัว}");
					}
				}
			} else if(type == OrderTypeConstant.TYPE2.getId() ||
					type == OrderTypeConstant.TYPE21.getId()) {

				if(bon2 != null) {
					if(bon2.contains(orderNumber)) {
						throw new CustomerException(2, orderNumber + " in restricted number {2 ตัวบน}");
					}
				}
				if(all != null) {
					if(all.contains(orderNumber)) {
						throw new CustomerException(4, orderNumber + " in restricted number {2 ตัวบน และ 2 ตัวล่าง}");
					}
				}
			} else if(type == OrderTypeConstant.TYPE3.getId() ||
					type == OrderTypeConstant.TYPE31.getId()) {

				if(lang2 != null) {
					if(lang2.contains(orderNumber)) {
						throw new CustomerException(3, orderNumber + " in restricted number {2 ตัวล่าง}");
					}
				}
				if(all != null) {
					if(all.contains(orderNumber)) {
						throw new CustomerException(4, orderNumber + " in restricted number {2 ตัวบน และ 2 ตัวล่าง}");
					}
				}
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private List<Map> reFormat(Receiver rc, List<Map> oldResult, List<Map> newResult, String key, List<Users> users) {
		List<Map> resultList = new ArrayList<>();
		Map<String, Object> resultMap;
		Object price, todPrice;
		Map map, oldMap;
		ObjectId userId;
		String name, oldName, userName, oldUserName;

		outer: for (int i = 0; i < newResult.size(); i++) {
			resultMap = new HashMap<>();
			map = newResult.get(i);
			userName = "-";
			todPrice = 0;

			price = map.get("price");
			if(map.containsKey("todPrice")) {
				todPrice = map.get("todPrice");
			}

			userId = (ObjectId)map.get("userId");
			name = (String)map.get("name");

			for (int j = 0; j < users.size(); j++) {
				if(users.get(j).getId().toString().equals(userId.toString())) {
					userName = users.get(j).getShowname();
					break;
				}
			}

			if(oldResult != null) {
				for(int j = 0; j < oldResult.size(); j++) {
					oldMap = oldResult.get(j);
					oldUserName = (String)oldMap.get("user");
					oldName = (String)oldMap.get("name");

					if(oldUserName.equals(userName) && oldName.equals(name)) {
						oldMap.put(key + "_price", price);
						oldMap.put(key + "_todPrice", todPrice);
						continue outer;
					}
				}
			}

			resultMap.put(key + "_price", price);
			resultMap.put(key + "_todPrice", todPrice);
			resultMap.put("user", userName);
			resultMap.put("name", name);
			resultMap.put("receiverName", rc.getReceiverName());

			resultList.add(resultMap);
		}

		if(oldResult == null) {
			oldResult = resultList;
		} else {
			oldResult.addAll(resultList);
		}

		return oldResult;
	}

	private Map<String, List<Map>> checkResult(Receiver rc, String periodId, String result3, String result2, String receiverId, String dealerId) {
		try {
			Map<String, List<Map>> resultMap = new HashMap<>();

			UserSearchCriteriaReq userReq = new UserSearchCriteriaReq();
			userReq.setDealerId(dealerId);
			List<Users> users = userService.getUsers(userReq);

			//-----------: 3 ตัวบน
			List<Integer> typeLst = Arrays.asList(new Integer[] { 1, 11, 12, 13, 14 });
			List<Map> result = chkLot(typeLst, periodId, result3, 1, receiverId, dealerId, false);
			result = reFormat(rc, null, result, "result3", users);

			//-----------: โต๊ด
			typeLst = Arrays.asList(new Integer[] { 13, 14, 131 });
			result = reFormat(rc, result, chkLot(typeLst, periodId, result3, 1, receiverId, dealerId, true), "resultTod", users);

			//-----------: 2 ตัวบน
			typeLst = Arrays.asList(new Integer[] { 2, 21 });
			result = reFormat(rc, result, chkLot(typeLst, periodId, result3.substring(1), 1, receiverId, dealerId, false), "resultBon2", users);

			//-----------: 2 ตัวล่าง
			typeLst = Arrays.asList(new Integer[] { 3, 31 });
			result = reFormat(rc, result, chkLot(typeLst, periodId, result2, 1, receiverId, dealerId, false), "resultLang2", users);

			//-----------: ลอย / แพ / วิ่ง
			typeLst = Arrays.asList(new Integer[] { 4, 41, 42, 43, 44 });
			List<Map> resultChk2 = chkLot(typeLst, periodId, null, 2, receiverId, dealerId, false);
			List<Map> loy = new ArrayList<>();
			List<Map> pair4 = new ArrayList<>();
			List<Map> pair5 = new ArrayList<>();
			List<Map> runBon = new ArrayList<>();
			List<Map> runLang = new ArrayList<>();
			Map<String, Object> loyMap;
			String orderNumber;
			int type;
			int countMatch;
			for (Map map : resultChk2) {
				orderNumber = map.get("orderNumber").toString();
				type = (int)map.get("type");
				countMatch = 0;

				if(type == 4 || type == 41 || type == 42) {
					for (int i = 0; i < orderNumber.length(); i++) {
						if(result3.contains(String.valueOf(orderNumber.charAt(i)))) {
							countMatch++;
						}
					}

					if(type == 4 && countMatch == 1) {
						loyMap = new HashMap<>();
						loyMap.put("userId", map.get("userId"));
						loyMap.put("name", map.get("name"));
						loyMap.put("orderNumber", orderNumber);
						loyMap.put("price", String.format("%,.0f", map.get("price")));
						loy.add(loyMap);
					} else if((type == 41 || type == 42) && countMatch >= 3) {
						loyMap = new HashMap<>();
						loyMap.put("userId", map.get("userId"));
						loyMap.put("name", map.get("name"));
						loyMap.put("orderNumber", orderNumber);
						loyMap.put("price", String.format("%,.0f", map.get("price")));

						if(type == 41) pair4.add(loyMap); else pair5.add(loyMap);
					}
				} else if(type == 43) {
					for (int i = 0; i < orderNumber.length(); i++) {
						if(result3.contains(String.valueOf(orderNumber.charAt(i)))) {
							countMatch++;
						}
					}
					if(countMatch == 2) {
						loyMap = new HashMap<>();
						loyMap.put("userId", map.get("userId"));
						loyMap.put("name", map.get("name"));
						loyMap.put("orderNumber", orderNumber);
						loyMap.put("price", String.format("%,.0f", map.get("price")));
						runBon.add(loyMap);
					}
				} else if(type == 44) {
					for (int i = 0; i < orderNumber.length(); i++) {
						if(result2.contains(String.valueOf(orderNumber.charAt(i)))) {
							countMatch++;
						}
					}
					if(countMatch == 1) {
						loyMap = new HashMap<>();
						loyMap.put("userId", map.get("userId"));
						loyMap.put("name", map.get("name"));
						loyMap.put("orderNumber", orderNumber);
						loyMap.put("price", String.format("%,.0f", map.get("price")));
						runLang.add(loyMap);
					}
				}
			}

			result = reFormat(rc, result, loy, "loy", users);
			result = reFormat(rc, result, pair4, "pair4", users);
			result = reFormat(rc, result, pair5, "pair5", users);
			result = reFormat(rc, result, runBon, "runBon", users);
			result = reFormat(rc, result, runLang, "runLang", users);

			resultMap.put("result", result);
			return resultMap;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private List<Map> chkLot(List<Integer> typeLst, String periodId, String lotResult,
								int queryType, String receiverId, String dealerId, boolean isChkTod) {

		MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);
		List<Map> result;

		if(queryType == 1) {
			Criteria criteria = Criteria.where("type").in(typeLst).and("periodId").is(new ObjectId(periodId)).and("orderNumber").is(lotResult);

			if(!StringUtils.isBlank(receiverId)) {
				if(isChkTod) {
					criteria.and("todReceiverId").is(new ObjectId(receiverId));
				} else {
					criteria.and("receiverId").is(new ObjectId(receiverId));
				}
			}

			BasicDBObject group = new BasicDBObject();
			group.append("userId", "$userId");
			group.append("name", "$name");

			Aggregation agg = Aggregation.newAggregation(
					Aggregation.match(criteria),
					new CustomAggregationOperation(
							new BasicDBObject(
									"$group",
									new BasicDBObject("_id", group)
									.append("price", new BasicDBObject("$sum", "$price"))
									.append("todPrice", new BasicDBObject("$sum", "$todPrice"))
									)
							),
					Aggregation.sort(Sort.Direction.DESC, "price")
			);
			AggregationResults<Map> aggregate = dealerTemp.aggregate(agg, "order", Map.class);

			result = aggregate.getMappedResults();
		} else {
			Criteria criteria = Criteria.where("type").in(typeLst).and("periodId").is(new ObjectId(periodId));

			if(!StringUtils.isBlank(receiverId)) {
				criteria.and("receiverId").is(new ObjectId(receiverId));
			}
			Query query = Query.query(criteria);

			query.fields()
			.include("orderNumber").include("name").include("price")
			.include("type").include("userId");

			query.with(new Sort(Sort.Direction.DESC, "userId", "price"));

			result = dealerTemp.find(query, Map.class, "order");
		}
		return result;
	}

	private byte[] zipFile(byte[] byteArr, String period) throws Exception {
		try {
			String tmpDir = String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS%1$tL", Calendar.getInstance().getTime()) + "/";
			String orderFile = basePath + "/orderfile/" + tmpDir;
			new File(orderFile).mkdir();

			PDDocument document = PDDocument.load(byteArr);
			int numberOfPages = document.getNumberOfPages();
			PDFRenderer renderer = new PDFRenderer(document);
			BufferedImage image;

			for (int i = 0; i < numberOfPages; i++) {
				image = renderer.renderImageWithDPI(i, 300);

				//-- Save to .jpg file
				ImageIO.write(image, "JPEG", new File(orderFile + period + "_" + (i+1) + ".jpg"));
			}

			//-- Save to .pdf file
			FileUtils.writeByteArrayToFile(new File(orderFile + period + ".pdf"), byteArr);

			//-- Create .zip byte[] data
			byte[] zipByte = ZipUtil.createZip(orderFile);

			// Remove all file.
//			FileUtils.cleanDirectory(new File(orderFile));
			FileUtils.deleteDirectory(new File(orderFile));

			return zipByte;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private List<String> formatedOrder(List<String> list) {
		try {
			List<String> result = new ArrayList<>();
			StringBuilder formated1 = new StringBuilder();
			StringBuilder formated2 = new StringBuilder();
			int pageIndex = 0;
			int rowSize = 40;
			String str;

			for (int i = 0; i < list.size(); i++) {
				str = list.get(i);
				if(pageIndex < rowSize) {
					formated1.append(str);
				} else if(pageIndex < rowSize * 2){
					formated2.append(str);
				} else {
					pageIndex = 0;
					formated1.append(str);
				}
				pageIndex++;
			}

			result.add(formated1.toString().trim());
			result.add(formated2.toString().trim());

			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private byte[] mergePdf(byte[] pdfByte1, byte[] pdfByte2) throws Exception {
		try {
			Document document = new Document();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			PdfCopy copy = new PdfSmartCopy(document, outputStream);
			document.open();

			PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfByte1));
			copy.addDocument(reader);

			reader = new PdfReader(new ByteArrayInputStream(pdfByte2));
			copy.addDocument(reader);

			reader.close();
			document.close();

			pdfByte1 = outputStream.toByteArray();

			return pdfByte1;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private List<Map> getData2(String periodId, String userId, List<Integer> type, String receiverId, String dealerId) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);
			Criteria criteria = Criteria.where("type").in(type).and("periodId").is(new ObjectId(periodId));

			if(!StringUtils.isBlank(userId)) {
				criteria.and("userId").is(new ObjectId(userId));
			}
			if(!StringUtils.isBlank(receiverId)) {
				criteria.and("receiverId").is(new ObjectId(receiverId));
			}

			Aggregation agg = Aggregation.newAggregation(
					Aggregation.match(criteria),
					new CustomAggregationOperation(
					        new BasicDBObject(
					            "$group",
					            new BasicDBObject("_id", "$orderNumber")
					            .append("price", new BasicDBObject("$sum", "$price"))
					        )
						),
					Aggregation.sort(Sort.Direction.DESC, "price")
			);

			AggregationResults<Map> aggregate = dealerTemp.aggregate(agg, "order", Map.class);
			return aggregate.getMappedResults();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private List<OrderCriteriaReq> translateOrdNumber(OrderCriteriaReq req, String orderNumber, Integer type, Double price, Double priceDesc) throws Exception {
		try {
			List<OrderCriteriaReq> orderReqList = new ArrayList<>();
			req.setOrderNumber(orderNumber);
			OrderCriteriaReq clone = (OrderCriteriaReq)req.clone();

			// 1 Digit.
			if(orderNumber.length() == 1) {
				if(type == null || type.intValue() == 4) { // If type is null so will be Loy
					clone.setLoy(price);
				} else {
					if(type.intValue() == 44) { // Run lang.
						clone.setRunLang(price);
					} else {
						throw new Exception("This type isn't SUPPORT!!! {" + type +"}");
					}
				}
				orderReqList.add(clone);
			}

			// 2 Digit.
			else if(orderNumber.length() == 2) {
				if(type == null || type.intValue() == 23 || type.intValue() == 2 || type.intValue() == 3) { // Both Bon and Lang.
					if(type.intValue() == 2) {
						clone.setBon(price);
					} else if(type.intValue() == 3) {
						clone.setLang(price);
					} else {
						clone.setBon(price);
						clone.setLang(price);
					}

					if(priceDesc != null) {
						if(price.doubleValue() == priceDesc.doubleValue()) {
							if(type.intValue() == 2) {
								clone.setBonSw(true);
							} else if(type.intValue() == 3) {
								clone.setLangSw(true);
							} else {
								clone.setBonSw(true);
								clone.setLangSw(true);
							}
							orderReqList.add(clone);
						} else {
							orderReqList.add(clone);

							clone = (OrderCriteriaReq)req.clone();
							clone.setOrderNumber(getOrderNumProb(orderNumber).get(1));

							if(type.intValue() == 2) {
								clone.setBon(priceDesc);
							} else if(type.intValue() == 3) {
								clone.setLang(priceDesc);
							} else {
								clone.setBon(priceDesc);
								clone.setLang(priceDesc);
							}
							orderReqList.add(clone);
						}
					} else {
						orderReqList.add(clone);
					}
				} else if(type.intValue() == 43) {
					clone.setRunBon(price);
					orderReqList.add(clone);
				} else {
					throw new Exception("This type isn't SUPPORT!!! {" + type +"}");
				}
			}

			// 3 Digit.
			else if(orderNumber.length() == 3) {
				clone.setBon(price);
				if(priceDesc != null) {
					if(priceDesc.doubleValue() > 6) {
						clone.setTod(priceDesc);
					} else {
						clone.setBonSw(true);
					}
				}
				orderReqList.add(clone);
			}

			// 4 Digit OR 5 Digit.
			else if(orderNumber.length() == 4 || orderNumber.length() == 5) {
				if(priceDesc == null) {
					clone.setLoy(price);
				} else {
					clone.setBon(price);
				}
				orderReqList.add(clone);
			}

			else {
				throw new Exception("Order Number length isn't support {" + orderNumber + "}");
			}

			return orderReqList;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	/*public static void main(String[] args) throws Exception {
	try {
		Map<Object, Object> hashMap = new HashMap<>();
		hashMap.put("period", "1111");

		List<Map> data = new ArrayList<>();
		data.add(hashMap);

		byte b[] = new JasperReportEngine().toPdf("C:\\Users\\LENOVO\\Desktop\\jasper\\order.jasper", data, null);
		byte c[] = new JasperReportEngine().toPdf("C:\\Users\\LENOVO\\Desktop\\jasper\\order.jasper", data, null);
		b = new OrderService(null).mergePdf(b, c);

		PDDocument document = PDDocument.load(b);
		PDFRenderer renderer = new PDFRenderer(document);
		int numberOfPages = document.getNumberOfPages();
		for (int i = 0; i < numberOfPages; i++) {
			BufferedImage image = renderer.renderImageWithDPI(i, 150);
			ImageIO.write(image, "JPEG", new File("D:/order/order" + i + ".jpg"));
		}
		ZipUtil.createZip("D:/order", "D:/order/test.zip");
	} catch (Exception e) {
		LOG.error(e.toString());
		throw e;
	}
}*/

}
