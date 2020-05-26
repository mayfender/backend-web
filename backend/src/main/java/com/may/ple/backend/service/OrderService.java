package com.may.ple.backend.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.constant.OrderTypeConstant;
import com.may.ple.backend.criteria.OrderCriteriaReq;
import com.may.ple.backend.entity.Order;
import com.may.ple.backend.entity.Period;

@Service
public class OrderService {
	private static final Logger LOG = Logger.getLogger(OrderService.class.getName());
	private MongoTemplate template;
	
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
	/**
	 * 
	 * @param req
	 * @throws Exception
	 * {type} 
	 * 1 = 3 ตัวบน, 11 = กลับ 3, 12 = กลับ 6
	 * 2 = 2 ตัวบน, 21 = 2 ตัวบนกลับ
	 * 3 = 2 ตัวล่าง, 31 = 2 ตัวล่างกลับ
	 * 4 = โต๊ด
	 * 5 = ลอย
	 */
	
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
				Double childPrice = 0.0;
				
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
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<Map> getPeriod(String userId) {
		try {
			Query query = new Query();
			query.with(new Sort(Sort.Direction.DESC, "periodDateTime"));
			
			return template.find(query, Map.class, "period");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
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
				order.setTodPrice(childPriceDummy);
			} else {
				order.setParentId(id);							
				order.setIsChild(true);
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
				order.setIsChild(true);
				
				objLst.add(order);	
			}
		}		
		
		return objLst;
	}
	
}
