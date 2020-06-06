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
import com.may.ple.backend.utils.ZipUtil;
import com.mongodb.BasicDBObject;

@Service
public class OrderService {
	private static final Logger LOG = Logger.getLogger(OrderService.class.getName());
	private MongoTemplate template;
	@Value("${file.path.base}")
	private String basePath;
	
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
							parentType, req.getBon(), req.getBon(), req.getUserId(), req.getPeriodId(), null));
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
							parentType, req.getLang(), req.getLang(), req.getUserId(), req.getPeriodId(), null));
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
						req.getBon(), childPrice, req.getUserId(), req.getPeriodId(), null));				
			} else if(req.getOrderNumber().length() > 3 && req.getBon() != null) {
				orderNumProb = getOrderNumProbOver3(req.getOrderNumber());
				parentType = childType = OrderTypeConstant.TYPE12.getId();
				childPrice = req.getBon();				
				
				//---------
				objLst.addAll(prepareDbObj(orderNumProb, req.getName(), parentType, childType, 
						req.getBon(), childPrice, req.getUserId(), req.getPeriodId(), req.getOrderNumber()));
			}
			
			if(req.getLoy() != null) {
				parentType = OrderTypeConstant.TYPE4.getId();
				orderNumProb = new ArrayList<>();
				orderNumProb.add(req.getOrderNumber());
				
				objLst.addAll(prepareDbObj(orderNumProb, req.getName(), parentType, 
						parentType, req.getLoy(), null, req.getUserId(), req.getPeriodId(), null));
			}
			
			template.insert(objLst, "order");
			
			//---------------------------------------------------
			Query query = Query.query(Criteria.where("userId").is(new ObjectId(req.getUserId())));
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
		Integer[] spam = new Integer[] { 1 , 11 , 12 , 13 , 14 , 2, 21, 3, 31, 4 };
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
	
	private List<Map> getData1(String periodId, String userId) {
		try {
			Integer[] typeArr = new Integer[] { 1 , 11 , 12 , 13 , 14 , 4 };
			List<Integer> typeLst = Arrays.asList(typeArr);
			
			Criteria criteria = Criteria.where("type").in(typeLst).and("periodId").is(new ObjectId(periodId)).and("userId").is(new ObjectId(userId));
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
	
	public byte[] exportData(String periodId, String userId, Date periodDate) throws Exception {
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
			List<Map> orders = getData1(periodId, userId);
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
				} else if(type == OrderTypeConstant.TYPE4.getId()) {
					price = (double)order.get("price");
					loyChk +=price;
					
					priceStr = String.format("%,.0f", price);
					ordFormated = "ลอย " + orderNumber + " = " + priceStr + "\n";
					ordFormatedLstLoy.add(ordFormated);
				}	
			}
			
			// 2 ตัวบน
			List<Integer> typeLst = Arrays.asList(new Integer[] { 2, 21});
			
			//---: getData
			orders = getData2(periodId, userId,  typeLst);
			
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
			orders = getData2(periodId, userId,  typeLst);
			
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
			group.add("พี่ฝัน 3 ตัวตรง");
			group.add("พี่ฝัน 2 ตัวบน");
			group.add("พี่ฝัน 2 ตัวล่าง");
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
	
	public OrderCriteriaResp checkResult(String periodId) {
		try {
			OrderCriteriaResp resp = new OrderCriteriaResp();
			
			Query query = Query.query(Criteria.where("_id").is(new ObjectId(periodId)));
			Map period = template.findOne(query, Map.class, "period");
			String result2 = period.get("result2") == null ? null : period.get("result2").toString();
			String result3 = period.get("result3") == null ? null : period.get("result3").toString();
			
			if(StringUtils.isBlank(result2) || StringUtils.isBlank(result3)) return resp;
			
			//-----------: 3 ตัวบน
			List<Integer> typeLst = Arrays.asList(new Integer[] { 1, 11, 12, 13, 14 });
			List<Map> result = chkLot(typeLst, periodId, result3, 1);
			resp.setResult3(result);
			
			//-----------: โต๊ด
			typeLst = Arrays.asList(new Integer[] { 13, 14, 131 });
			result = chkLot(typeLst, periodId, result3, 1);
			resp.setResultTod(result);
			
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
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	/**
	 * Get data on time line that input to system.
	 */
	public List<Map> getDataOnTL(String periodId, String userId, String orderName) {
		try {
			List<Integer> typeLst = Arrays.asList(new Integer[] { 1 , 11 , 12 , 13 , 14 , 2, 21, 3, 31, 4 });
			
			Criteria criteria = Criteria.where("periodId").is(new ObjectId(periodId))
			.and("userId").is(new ObjectId(userId))
			.and("type").in(typeLst).and("isParent").is(true);
			
			if(!StringUtils.isBlank(orderName)) {
				criteria.and("name").is(orderName);
			}
			
			Query query = Query.query(criteria);
			
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
				} else {
					LOG.error("Out of if case.");
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
	
	//-----------------------: Private :------------------------------
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
			Integer childType, Double parentPrice, Double childPrice, String userId, String periodId, String orderNumberAlias) {
		
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
			
			objLst.add(order);	
		}
		
		if(parentType.intValue() == 14) {
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
									.append("todPrice", new BasicDBObject("$sum", "$todPrice"))
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
	
	private List<Map> getData2(String periodId, String userId, List<Integer> type) {
		try {
			Criteria criteria = Criteria.where("type").in(type).and("periodId").is(new ObjectId(periodId)).and("userId").is(new ObjectId(userId));
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
