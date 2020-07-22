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
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.Order;
import com.may.ple.backend.entity.OrderName;
import com.may.ple.backend.entity.Period;
import com.may.ple.backend.entity.Receiver;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.utils.ZipUtil;
import com.mongodb.BasicDBObject;

@Service
public class OrderService {
	private static final Logger LOG = Logger.getLogger(OrderService.class.getName());
	private ReceiverService settingService;
	private MongoTemplate template;
	@Value("${file.path.base}")
	private String basePath;

	@Autowired
	public OrderService(MongoTemplate template, ReceiverService settingService) {
		this.template = template;
		this.settingService = settingService;
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
			Query query = Query.query(Criteria.where("enabled").is(true));
			query.with(new Sort("order"));
			query.fields().include("id");

			Receiver firstReceiver = template.findOne(query, Receiver.class);

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

			template.insert(objLst, "order");

			//---------------------------------------------------
			query = Query.query(Criteria.where("userId").is(new ObjectId(req.getUserId())));
			OrderName orderName = template.findOne(query, OrderName.class, "orderName");

			if(orderName == null) {
				LOG.debug("Empty OrderName");
				List<Map> names = new ArrayList<>();
				Map<String, String> name = new HashMap<>();
				name.put("name", req.getName());
				names.add(name);

				orderName = new OrderName();
				orderName.setUserId(new ObjectId(req.getUserId()));
				orderName.setNames(names);

				template.save(orderName);
			} else if(orderName != null) {
				LOG.debug("Existing OrderName");
				Update update = new Update();
				update.addToSet("names", new BasicDBObject("name", req.getName()));

				query = Query.query(Criteria.where("_id").is(new ObjectId(orderName.getId())));
				template.updateFirst(query, update, "orderName");
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void editDelete(OrderCriteriaReq req) {
		try {
			if(StringUtils.isBlank(req.getOrderNameUpdate())) {
				LOG.info("Delete");
				Criteria criteria1 = Criteria.where("_id").is(new ObjectId(req.getOrderId()));
				Criteria criteria2 = Criteria.where("parentId").is(new ObjectId(req.getOrderId()));

				Query query = Query.query(new Criteria().orOperator(criteria1, criteria2));
				template.remove(query, "order");
			} else {
				LOG.info("Update Name");

				Criteria criteria1 = Criteria.where("_id").is(new ObjectId(req.getOrderId()));
				Criteria criteria2 = Criteria.where("parentId").is(new ObjectId(req.getOrderId()));

				Query query = Query.query(new Criteria().orOperator(criteria1, criteria2));

				Update update = new Update();
				update.set("name", req.getOrderNameUpdate());

				template.updateMulti(query, update, "order");
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

	public List<Map> getSumOrder(String tab, List<Integer> type, String orderName, String periodId, String userId, String receiverId) {
		Criteria criteria = Criteria.where("type").in(type).and("periodId").is(new ObjectId(periodId)).and("userId").is(new ObjectId(userId));

		if(!StringUtils.isBlank(orderName)) {
			criteria.and("name").is(orderName);
		}
		if(!StringUtils.isBlank(receiverId)) {
			criteria.and("receiverId").is(new ObjectId(receiverId));
		}

		String priceField;
		if(tab.equals("5")) {
			priceField = "$todPrice";
		} else {
			priceField = "$price";
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

		AggregationResults<Map> aggregate = template.aggregate(agg, "order", Map.class);
		List<Map> mappedResults = aggregate.getMappedResults();

		return mappedResults;
	}

	public Map getSumOrderTotal(String orderName, String periodId, String userId, String receiverId, List<Integer> typeLst) {
//		Integer[] spam = new Integer[] { 1 , 11 , 12 , 13 , 14 , 2, 21, 3, 31, 4 };
//		List<Integer> type = Arrays.asList(spam);

		Criteria criteria = Criteria.where("type").in(typeLst).and("periodId").is(new ObjectId(periodId)).and("userId").is(new ObjectId(userId));

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

		AggregationResults<Map> aggregate = template.aggregate(agg, "order", Map.class);
		Map result = aggregate.getUniqueMappedResult();

		return result;
	}

	public OrderName getOrderName(String userId, String prefix) {
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

			OrderName orderName = template.findOne(Query.query(Criteria.where("userId").is(new ObjectId(userId))), OrderName.class, "orderName");
			return orderName;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List getOrderNameByPeriod(String userId, String periodId) {
		try {
			BasicDBObject dbObject = new BasicDBObject();
			dbObject.append("userId", new ObjectId(userId));
			dbObject.append("periodId", new ObjectId(periodId));

			return template.getCollection("order").distinct("name", dbObject);
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

	private List<Map> getData1(String periodId, String userId, String receiverId) {
		try {
			List<Integer> typeLst = getGroup("145678", true);

			Criteria criteria = Criteria.where("type").in(typeLst).and("periodId").is(new ObjectId(periodId)).and("userId").is(new ObjectId(userId));

			if(!StringUtils.isBlank(receiverId)) {
				criteria.and("receiverId").is(new ObjectId(receiverId));
			}

			Query query = Query.query(criteria);
			query.fields()
			.include("orderNumber")
			.include("orderNumberAlias")
			.include("type")
			.include("probNum")
			.include("price")
			.include("isParent")
			.include("todPrice");
			query.with(new Sort(Sort.Direction.DESC, "price"));

			return template.find(query, Map.class, "order");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public byte[] exportData(String periodId, String userId, Date periodDate, String receiverId, Receiver receiver) throws Exception {
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
			List<Map> orders = getData1(periodId, userId, receiverId);
			double bon3Chk = 0, loyChk = 0, bon2Chk = 0, lang2Chk = 0, todChk = 0;
			double price, todPrice;
			int probNum;

			for (int i = 0; i < orders.size(); i++) {
				order = orders.get(i);
				type = (int)order.get("type");
				isParent = (boolean)order.get("isParent");
//				orderNumber = StringUtils.leftPad(order.get("orderNumber").toString(), 10);
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
						price = (double)order.get("price");
						todPrice = (double)order.get("todPrice");
						bon3Chk += price;
						todChk += todPrice;

						priceStr = String.format("%,.0f", price);
						todPriceStr = String.format("%,.0f", todPrice);
						ordFormated = orderNumber + " = " + priceStr + "x" + todPriceStr + "\n";
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
					loyChk += price;

					String label = "";
					if(type == OrderTypeConstant.TYPE4.getId()) {
						label = "ลอย";
					} else if(type == OrderTypeConstant.TYPE41.getId() ||
							  type == OrderTypeConstant.TYPE42.getId()) {
						label = "แพ";
					} else if(type == OrderTypeConstant.TYPE43.getId()) {
						label = "วิ่งบน";
					} else if(type == OrderTypeConstant.TYPE44.getId()) {
						label = "วิ่งล่าง";
					}

					priceStr = String.format("%,.0f", price);
					ordFormated = label + " " + orderNumber + " = " + priceStr + "\n";
					ordFormatedLstLoy.add(ordFormated);
				}
			}

			// 2 ตัวบน
			List<Integer> typeLst = Arrays.asList(new Integer[] { 2, 21});

			//---: getData
			orders = getData2(periodId, userId,  typeLst, receiverId);

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
			orders = getData2(periodId, userId,  typeLst, receiverId);

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
			String bon3ChkStr, bon2ChkStr, lang2ChkStr, loyChkStr, todChkStr;

			bon3ChkStr = String.format("%,.0f", bon3Chk);
			bon2ChkStr = String.format("%,.0f", bon2Chk);
			lang2ChkStr = String.format("%,.0f", lang2Chk);
			loyChkStr = String.format("%,.0f", loyChk);
			todChkStr = String.format("%,.0f", todChk);

			LOG.info("3 ตัวบน: " + bon3ChkStr + ", 2 ตัวบน: " + bon2ChkStr + ", 2 ตัวล่าง: " + lang2ChkStr + ", loy: " + loyChkStr + ", โต๊ด: " + todChkStr);
			//---------: Checking :------

			return zipFile(pdfByte, period);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public OrderCriteriaResp checkResult(String periodId, Boolean isAllReceiver) {
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
				List<Receiver> receiverList = settingService.getReceiverList(true);

				for (Receiver rc : receiverList) {
					chkResultMap = checkResult(periodId, result3, result2, rc.getId());
					multiRc.put(rc.getId(), chkResultMap);
				}
			} else {
				chkResultMap = checkResult(periodId, result3, result2, null);
				multiRc.put("total", chkResultMap);
			}

			resp.setChkResultMap(multiRc);

			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	/**
	 * Get data on time line that input to system.
	 */
	public List<Map> getDataOnTL(String periodId, String userId, String orderName, List<Integer> typeLst, String receiverId, Sort sort) {
		try {
			Criteria criteria = Criteria.where("periodId").is(new ObjectId(periodId))
			.and("userId").is(new ObjectId(userId))
			.and("type").in(typeLst).and("isParent").is(true);

			if(!StringUtils.isBlank(orderName)) {
				criteria.and("name").is(orderName);
			}
			if(!StringUtils.isBlank(receiverId)) {
				criteria.and("receiverId").is(new ObjectId(receiverId));
			}

			Query query = Query.query(criteria);
			if(sort != null) query.with(sort);

			List<Map> orderLst = template.find(query, Map.class, "order");
			String orderNumber, symbol = "", note = "";
			int type, probNum;
			String price;

			for (Map order : orderLst) {
				orderNumber = order.get("orderNumber").toString();
				type = (int)order.get("type");
				probNum = (int)order.get("probNum");
				price = String.format("%,.0f", order.get("price"));

				if(type == 2) {
					note = "บน";
				} else if(type == 3) {
					note = "ล่าง";
				} else if(type == 21) {
					symbol = " x " + probNum;
					note = "บน";
				} else if(type == 31) {
					symbol = " x " + probNum;
					note = "ล่าง";
				} else if(type == 11) {
					symbol = " x " + probNum;
				} else if(type == 12) {
					orderNumber = order.get("orderNumberAlias").toString();
					symbol = " x " + probNum;
				} else if(type == 13) {
					symbol = " x " + String.format("%,.0f", order.get("todPrice"));
				} else if(type == 14) {
					symbol = " x " + probNum + " x " + String.format("%,.0f", order.get("todPrice"));
				} else if(type == 4) {
					note = "ลอย";
				} else if(type == 41) {
					note = "แพ 4";
				} else if(type == 42) {
					note = "แพ 5";
				} else if(type == 43) {
					note = "วิ่งบน";
				} else if(type == 44) {
					note = "วิ่งล่าง";
				} else {
					LOG.debug("type: " + type);
				}

				order.put("symBol", orderNumber + " = " + price + symbol);
				order.put("note", note);

				symbol = "";
				note = "";
			}

			return orderLst;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void moveToReceiver(OrderCriteriaReq req) throws Exception {
		try {
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
			.include("receiverId");

			List<Order> orders = template.find(query, Order.class);

			for (Order order : orders) {
				if(order.getReceiverId().toString().equals(req.getReceiverId())) throw new Exception();

				try {
					LOG.debug("Start call restrictedCheck noPrice");
					restrictedCheck(order.getType(), noPrice, order.getOrderNumber());
				} catch (CustomerException e) {
					LOG.warn(order.getOrderNumber() + " is in restriction number.");
					throw e;
				}
			}

			Update update = new Update();
			update.set("receiverId", new ObjectId(req.getReceiverId()));
			template.updateMulti(query, update, Order.class);
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
				orderDataMainList = getDataOnTL(req.getPeriodId(), req.getUserId(), null, types, req.getMoveFromId(), null);
			} else {
				orderDataMainList = getDataOnTL(req.getPeriodId(), req.getUserId(), null, types, req.getMoveFromId(), new Sort(Sort.Direction.DESC, "price"));
			}

			if(orderDataMainList == null || orderDataMainList.size() == 0) return 0;

			LOG.debug("Start prepareRestrictedNumber");
			List<String> recIds = new ArrayList<>();
			recIds.add(req.getMoveToId());

			OrderCriteriaReq reqRest = new OrderCriteriaReq();
			reqRest.setPeriodId(req.getPeriodId());
			reqRest.setReceiverIds(recIds);

			Object restrictedOrderObj = prepareRestrictedNumber(getRestrictedOrder(reqRest), true).get(req.getMoveToId());
			Map noPrice = null;
			if(restrictedOrderObj != null) {
				Map restrictedOrderMap = (Map)restrictedOrderObj;
				noPrice = (Map)restrictedOrderMap.get("noPrice");
			}
			LOG.debug("End prepareRestrictedNumber");

			List<ObjectId> ids;
			if(req.getOperator().equals("1")) { // Less than or equal.

				LOG.debug("Get Move-to data");
				List<Map> sumOrderLst = getSumOrder(
					req.getTab(), types, null, req.getPeriodId(), req.getUserId(), req.getMoveToId()
				);

				Map<String, Double> sumOrderMoveTo = new HashMap<>();
				for (Map sumOrder : sumOrderLst) {
					sumOrderMoveTo.put(sumOrder.get("_id").toString(), Double.valueOf(sumOrder.get("totalPrice").toString()));
				}

				LOG.debug("call moveLte");
				ids = moveLte(req, orderDataMainList, types, sumOrderMoveTo);
			} else if(req.getOperator().equals("2")) { // Greater than or equal.
				LOG.debug("call moveGt");
				ids = moveAllOrGt(req, orderDataMainList, types, noPrice, false);
			} else { // All
				ids = moveAllOrGt(req, orderDataMainList, types, noPrice, true);
			}

			if(ids.size() == 0) return 0;

			LOG.debug("Changing receiver.");
			Update update = new Update();
			update.set("receiverId", new ObjectId(req.getMoveToId()));

			Query query = Query.query(new Criteria().orOperator(Criteria.where("_id").in(ids), Criteria.where("parentId").in(ids)));
			template.updateMulti(query, update, "order");

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
	private List<ObjectId> moveAllOrGt(OrderCriteriaReq req, List<Map> orderDataMainList, List<Integer> types, Map noPrice, boolean isAll) throws Exception {
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

					price = (double)data.get("price");
					orderNumber = data.get("orderNumber").toString();
					type = (int)data.get("type");

					probNum = (int)data.get("probNum");
					if(probNum > 1) {
						if(type != 13) {
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
				}
			} else {
				order.setParentId(id);
				order.setIsParent(false);
				order.setType(childType);
				if(childType.intValue() == OrderTypeConstant.TYPE131.getId()) {
					order.setTodPrice(childPrice);
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

	private Map<String, List<Map>> checkResult(String periodId, String result3, String result2, String receiverId) {
		try {
			Map<String, List<Map>> resultMap = new HashMap<>();

			//-----------: 3 ตัวบน
			List<Integer> typeLst = Arrays.asList(new Integer[] { 1, 11, 12, 13, 14 });
			List<Map> result = chkLot(typeLst, periodId, result3, 1, receiverId);
			resultMap.put("result3", result);

			//-----------: โต๊ด
			typeLst = Arrays.asList(new Integer[] { 13, 14, 131 });
			result = chkLot(typeLst, periodId, result3, 1, receiverId);
			resultMap.put("resultTod", result);

			//-----------: 2 ตัวบน
			typeLst = Arrays.asList(new Integer[] { 2, 21 });
			result = chkLot(typeLst, periodId, result3.substring(1), 1, receiverId);
			resultMap.put("resultBon2", result);

			//-----------: 2 ตัวล่าง
			typeLst = Arrays.asList(new Integer[] { 3, 31 });
			result = chkLot(typeLst, periodId, result2, 1, receiverId);
			resultMap.put("resultLang2", result);

			//-----------: ลอย / แพ / วิ่ง
			typeLst = Arrays.asList(new Integer[] { 4, 41, 42, 43, 44 });
			result = chkLot(typeLst, periodId, null, 2, receiverId);
			List<Map> loy = new ArrayList<>();
			List<Map> pair4 = new ArrayList<>();
			List<Map> pair5 = new ArrayList<>();
			List<Map> runBon = new ArrayList<>();
			List<Map> runLang = new ArrayList<>();
			Map<String, Object> loyMap;
			String orderNumber;
			int type;
			int countMatch;
			for (Map map : result) {
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
						loyMap.put("name", map.get("name"));
						loyMap.put("orderNumber", orderNumber);
						loyMap.put("price", String.format("%,.0f", map.get("price")));
						loy.add(loyMap);
					} else if((type == 41 || type == 42) && countMatch >= 3) {
						loyMap = new HashMap<>();
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
						loyMap.put("name", map.get("name"));
						loyMap.put("orderNumber", orderNumber);
						loyMap.put("price", String.format("%,.0f", map.get("price")));
						runLang.add(loyMap);
					}
				}
			}

			resultMap.put("loy", loy);
			resultMap.put("pair4", pair4);
			resultMap.put("pair5", pair5);
			resultMap.put("runBon", runBon);
			resultMap.put("runLang", runLang);

			return resultMap;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private List<Map> chkLot(List<Integer> typeLst, String periodId, String lotResult, int queryType, String receiverId) {
		List<Map> result;

		if(queryType == 1) {
			Criteria criteria = Criteria.where("type").in(typeLst).and("periodId").is(new ObjectId(periodId)).and("orderNumber").is(lotResult);

			if(!StringUtils.isBlank(receiverId)) {
				criteria.and("receiverId").is(new ObjectId(receiverId));
			}

			Aggregation agg = Aggregation.newAggregation(
					Aggregation.match(criteria),
					new CustomAggregationOperation(
							new BasicDBObject(
									"$group",
									new BasicDBObject("_id", "$name")
									.append("price", new BasicDBObject("$sum", "$price"))
									.append("todPrice", new BasicDBObject("$sum", "$todPrice"))
									)
							),
					Aggregation.sort(Sort.Direction.DESC, "price")
			);
			AggregationResults<Map> aggregate = template.aggregate(agg, "order", Map.class);

			result = aggregate.getMappedResults();
		} else {
			Criteria criteria = Criteria.where("type").in(typeLst).and("periodId").is(new ObjectId(periodId));

			if(!StringUtils.isBlank(receiverId)) {
				criteria.and("receiverId").is(new ObjectId(receiverId));
			}
			Query query = Query.query(criteria);

			query.fields().include("orderNumber").include("name").include("price").include("type");
			query.with(new Sort(Sort.Direction.DESC, "price"));

			result = template.find(query, Map.class, "order");
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

	private List<Map> getData2(String periodId, String userId, List<Integer> type, String receiverId) {
		try {
			Criteria criteria = Criteria.where("type").in(type).and("periodId").is(new ObjectId(periodId)).and("userId").is(new ObjectId(userId));

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

			AggregationResults<Map> aggregate = template.aggregate(agg, "order", Map.class);
			return aggregate.getMappedResults();
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
