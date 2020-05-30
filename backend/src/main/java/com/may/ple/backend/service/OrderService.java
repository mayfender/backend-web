package com.may.ple.backend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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

import com.may.ple.backend.bussiness.jasper.JasperReportEngine;
import com.may.ple.backend.constant.OrderTypeConstant;
import com.may.ple.backend.criteria.OrderCriteriaReq;
import com.may.ple.backend.criteria.OrderCriteriaResp;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.Order;
import com.may.ple.backend.entity.OrderName;
import com.may.ple.backend.entity.Period;
import com.mongodb.BasicDBObject;

@Service
public class OrderService {
	private static final Logger LOG = Logger.getLogger(OrderService.class.getName());
	private MongoTemplate template;
	@Value("${file.path.jasper}")
	private String jasperPath;
	
	@Autowired	
	public OrderService(MongoTemplate template) {
		this.template = template;
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
			List<Order> objLst = new ArrayList<>();
			List<String> orderNumProb = null;
			Integer parentType, childType;
			
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
							parentType, req.getBon(), req.getBon(), req.getUserId(), req.getPeriodId()));
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
							parentType, req.getLang(), req.getLang(), req.getUserId(), req.getPeriodId()));
				}
			} else if(req.getOrderNumber().length() == 3) {
				boolean isTod = req.getTod() != null;
				parentType = childType = OrderTypeConstant.TYPE1.getId();
				Double childPrice = null;
				
				if(req.getBonSw() || isTod) {
					orderNumProb = getOrderNumProb(req.getOrderNumber());
					
					if(req.getBonSw() && isTod) {
						if(orderNumProb.size() == 3) {
							parentType = OrderTypeConstant.TYPE14.getId();
						} else if(orderNumProb.size() == 6) {
							parentType = OrderTypeConstant.TYPE15.getId();
						}
						childPrice = req.getTod();
					} else if(req.getBonSw()) {
						childPrice = req.getBon();
						if(orderNumProb.size() == 3) {
							parentType = OrderTypeConstant.TYPE11.getId();
							childType = OrderTypeConstant.TYPE11.getId();
						} else if(orderNumProb.size() == 6) {
							parentType = OrderTypeConstant.TYPE12.getId();
							childType = OrderTypeConstant.TYPE12.getId();
						}
					} else if(isTod) {
						parentType = OrderTypeConstant.TYPE13.getId();
						childType = OrderTypeConstant.TYPE131.getId();
						childPrice = req.getTod();
					}
				} else {
					orderNumProb = new ArrayList<>();
					orderNumProb.add(req.getOrderNumber());
				}
				
				if(req.getBon() == null) return;
				
				//---------
				objLst.addAll(prepareDbObj(orderNumProb, req.getName(), parentType, childType, 
						req.getBon(), childPrice, req.getUserId(), req.getPeriodId()));				
			}
			
			if(req.getLoy() != null) {
				parentType = OrderTypeConstant.TYPE4.getId();
				orderNumProb = new ArrayList<>();
				orderNumProb.add(req.getOrderNumber());
				
				objLst.addAll(prepareDbObj(orderNumProb, req.getName(), parentType, 
						parentType, req.getLoy(), null, req.getUserId(), req.getPeriodId()));
			}
			
			template.insert(objLst, "order");
			
			//---------------------------------------------------
			Query query = Query.query(Criteria.where("userId").is(new ObjectId(req.getUserId())));
			OrderName orderName = template.findOne(query, OrderName.class, "orderName");
			
			if(orderName == null) {
				LOG.debug("Empty OrderName");
				List<Map> names = new ArrayList<>();
				Map name = new HashMap<>();
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
	
	public List<Map> getPeriod() {
		try {
			Query query = new Query();
			query.with(new Sort(Sort.Direction.DESC, "periodDateTime"));
			
			return template.find(query, Map.class, "period");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<Map> getSumOrder(String tab, List<Integer> type, String orderName, String periodId, String userId) {
		Criteria criteria = Criteria.where("type").in(type).and("periodId").is(new ObjectId(periodId)).and("userId").is(new ObjectId(userId));
		
		if(!StringUtils.isBlank(orderName)) {
			criteria.and("name").is(orderName);
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
	public Map getSumOrderTotal(String orderName, String periodId, String userId) {
		Integer[] spam = new Integer[] { 1 , 11 , 12 , 13 , 14 , 15, 2, 21, 3, 31, 4 };
		List<Integer> type = Arrays.asList(spam);
		
		Criteria criteria = Criteria.where("type").in(type).and("periodId").is(new ObjectId(periodId)).and("userId").is(new ObjectId(userId));
		
		if(!StringUtils.isBlank(orderName)) {
			criteria.and("name").is(orderName);
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
	
	public byte[] exportData(String periodId, String userId) throws Exception {
		try {
			Integer[] spam = new Integer[] { 1 , 11 , 12 , 13 , 14 , 15, 2, 21, 3, 31, 4 };
			List<Integer> typeLst = Arrays.asList(spam);
			
			Criteria criteria = Criteria.where("type").in(typeLst).and("periodId").is(new ObjectId(periodId)).and("userId").is(new ObjectId(userId));
			Query query = Query.query(criteria);
			query.fields()
			.include("orderNumber")
			.include("type")
			.include("probNum")
			.include("price")
			.include("isParent")
			.include("todPrice");
			query.with(new Sort(Sort.Direction.DESC, "price"));
			
			List<Map> orders = template.find(query, Map.class, "order");
			String ordFormated3 = "", ordFormated2 = "", ordFormatedLoy;
			List<String> ordFormatedLst3 = new ArrayList<>();
			List<String> ordFormatedLst2 = new ArrayList<>();
			List<String> ordFormatedLstLoy = new ArrayList<>();
			String priceStr, todPriceStr;
			boolean isParent;
			int type;
			Map order;
			
			for (int i = 0; i < orders.size(); i++) {
				order = orders.get(i);
				type = (int)order.get("type");
				isParent = (boolean)order.get("isParent");
				
				if(type == OrderTypeConstant.TYPE1.getId() ||
					type == OrderTypeConstant.TYPE11.getId() ||
					type == OrderTypeConstant.TYPE12.getId() ||
					type == OrderTypeConstant.TYPE13.getId() ||
					type == OrderTypeConstant.TYPE14.getId() ||
					type == OrderTypeConstant.TYPE15.getId()) {
					
					if(type == OrderTypeConstant.TYPE11.getId() && !isParent) continue;
					if(type == OrderTypeConstant.TYPE12.getId() && !isParent) continue;
					
					if(type == OrderTypeConstant.TYPE1.getId()) {
						priceStr = String.format("%,.0f", order.get("price"));
						ordFormated3 = order.get("orderNumber") + " = " + priceStr + "\n";	
					} else if(type == OrderTypeConstant.TYPE11.getId() ||
						type == OrderTypeConstant.TYPE12.getId()) {
						
						priceStr = String.format("%,.0f", order.get("price"));
						ordFormated3 = order.get("orderNumber") + " = " + priceStr + "x" + order.get("probNum") + "\n";
					} else if(type == OrderTypeConstant.TYPE13.getId()) {
						priceStr = String.format("%,.0f", order.get("price"));
						todPriceStr = String.format("%,.0f", order.get("todPrice"));
						
						ordFormated3 = order.get("orderNumber") + " = " + priceStr + "x" + todPriceStr + "\n";						
					} else if(type == OrderTypeConstant.TYPE14.getId() ||
						type == OrderTypeConstant.TYPE15.getId()) {
						
						priceStr = String.format("%,.0f", order.get("price"));
						todPriceStr = String.format("%,.0f", order.get("todPrice"));
						ordFormated3 = order.get("orderNumber") + " = " + priceStr + "x" + order.get("probNum") + "x" + todPriceStr + "\n";						
					} else {
						continue;
					}
					ordFormatedLst3.add(ordFormated3);
				} else if(type == OrderTypeConstant.TYPE2.getId() ||
						type == OrderTypeConstant.TYPE21.getId() ||
						type == OrderTypeConstant.TYPE3.getId() ||
						type == OrderTypeConstant.TYPE31.getId()){
					
					priceStr = String.format("%,.0f", order.get("price"));
					
					ordFormated2 = order.get("orderNumber") + " = " + priceStr + "\n";
					ordFormatedLst2.add(ordFormated2);
				} else if(type == OrderTypeConstant.TYPE4.getId()) {
					priceStr = String.format("%,.0f", order.get("price"));
					
					ordFormatedLoy = order.get("orderNumber") + " = " + priceStr + " ลอย \n";						
					ordFormatedLstLoy.add(ordFormatedLoy);
				}	
			}
			
			//-- Included Loy to 3;
			ordFormatedLst3.addAll(ordFormatedLstLoy);
			
			StringBuilder formated3_1 = new StringBuilder();
			StringBuilder formated3_2 = new StringBuilder();
			int pageIndex = 0;
			int rowSize = 40;
			for (int i = 0; i < ordFormatedLst3.size(); i++) {
				if(pageIndex < rowSize) {
					formated3_1.append(ordFormatedLst3.get(i));					
				} else if(pageIndex < rowSize * 2){
					formated3_2.append(ordFormatedLst3.get(i));				
				} else {
					pageIndex = 0;
					formated3_1.append(ordFormatedLst3.get(i));
				}
				pageIndex++;
			}
						
			//--------------------------------------------------
			//--- Fields
			Map<Object, Object> hashMap = new HashMap<>();
			hashMap.put("title", "3 ตัว");
			hashMap.put("order1", formated3_1.toString());
			hashMap.put("order2", formated3_2.toString());
			
			List<Map> taskDetails = new ArrayList<>();
			taskDetails.add(hashMap);
			
			String jasperFile = jasperPath + "/order.jasper";
			
			return new JasperReportEngine().toPdf(jasperFile, taskDetails, null);	
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public OrderCriteriaResp checkResult(String periodId) {
		try {
			OrderCriteriaResp resp = new OrderCriteriaResp();
			
			Query query = Query.query(Criteria.where("_id").is(new ObjectId(periodId)));
			Map period = template.findOne(query, Map.class, "period");
			String result2 = period.get("result2") == null ? null : period.get("result2").toString();
			String result3 = period.get("result3") == null ? null : period.get("result3").toString();
			
			//-----------: 3 ตัวบน
			List<Integer> typeLst = Arrays.asList(new Integer[] { 1, 11, 12, 13, 14, 15 });
			List<Map> result = chkLot(typeLst, periodId, result3, 1);
			resp.setResult3(result);
			
			//-----------: 2 ตัวบน
			typeLst = Arrays.asList(new Integer[] { 2, 21 });
			result = chkLot(typeLst, periodId, result3.substring(1), 1);
			resp.setResultBon2(result);
			
			//-----------: 2 ตัวล่าง
			typeLst = Arrays.asList(new Integer[] { 3, 31 });
			result = chkLot(typeLst, periodId, result2, 1);
			resp.setResultLang2(result);
			
			//-----------: ลอย
			typeLst = Arrays.asList(new Integer[] { 4 });
			result = chkLot(typeLst, null, result2, 2);
			
			//-----------: โต๊ด
			typeLst = Arrays.asList(new Integer[] { 131 });
			result = chkLot(typeLst, null, result2, 2);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
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
			Integer childType, Double parentPrice, Double childPrice, String userId, String periodId) {
		
		Date now = Calendar.getInstance().getTime();
		List<Order> objLst = new ArrayList<>();
		ObjectId id = null;
		Order order;
		Double childPriceDummy = childPrice;
		
		if(parentType.intValue() == 14) {
			childType = 11;
			childPrice = parentPrice;
		} else if(parentType.intValue() == 15) {
			childType = 12;			
			childPrice = parentPrice;
		}
		
		for (int i = 0; i < orderNumProb.size(); i++) {
			order = new Order();
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
				order.setIsParent(true);
				order.setType(parentType);
				order.setPrice(parentPrice);
				order.setProbNum(orderNumProb.size());
				
				if(parentType.intValue() == OrderTypeConstant.TYPE13.getId() ||
						parentType.intValue() == OrderTypeConstant.TYPE14.getId() ||
						parentType.intValue() == OrderTypeConstant.TYPE15.getId()) {
					order.setTodPrice(childPriceDummy);
				}
			} else {
				order.setParentId(id);							
				order.setIsParent(false);
				order.setType(childType);
				order.setPrice(childPrice);
			}
			
			objLst.add(order);	
		}
		
		if(parentType.intValue() == 14 || parentType.intValue() == 15) {
			LOG.debug("IsTod");
			orderNumProb.remove(0);
			childType = 131;
			childPrice = childPriceDummy;
			
			for (int i = 0; i < orderNumProb.size(); i++) {
				order = new Order();
				order.setCreatedDateTime(now);
				order.setName(name);
				order.setOrderNumber(orderNumProb.get(i));
				order.setType(childType);
				order.setPrice(childPrice);
				order.setUserId(new ObjectId(userId));
				order.setPeriodId(new ObjectId(periodId));
				order.setParentId(id);							
				order.setIsParent(false);
				
				objLst.add(order);	
			}
		}		
		
		return objLst;
	}
	
	private List<Map> chkLot(List<Integer> typeLst, String periodId, String lotResult, int queryType) {
		List<Map> result;
		
		if(queryType == 1) {
			Criteria criteria = Criteria.where("type").in(typeLst).and("periodId").is(new ObjectId(periodId)).and("orderNumber").is(lotResult);
			Aggregation agg = Aggregation.newAggregation(
					Aggregation.match(criteria),
					new CustomAggregationOperation(
							new BasicDBObject(
									"$group",
									new BasicDBObject("_id", "$name")
									.append("price", new BasicDBObject("$sum", "$price"))
									)
							),
					Aggregation.sort(Sort.Direction.DESC, "price")
			);			
			AggregationResults<Map> aggregate = template.aggregate(agg, "order", Map.class);
			
			result = aggregate.getMappedResults();
		} else {
			Query query = Query.query(Criteria.where("type").is(typeLst.get(0)));
			query.fields().include("orderNumber").include("name").include("price");
			query.with(new Sort(Sort.Direction.DESC, "price"));
			
			result = template.find(query, Map.class, "order");
		}
		return result;
	}
	
}
