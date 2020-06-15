package com.may.ple.backend.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.ibm.icu.util.Calendar;
import com.may.ple.backend.criteria.SettingCriteriaReq;
import com.may.ple.backend.entity.Receiver;

@Service
public class SettingService {
	private static final Logger LOG = Logger.getLogger(SettingService.class.getName());
	private MongoTemplate template;
	
	@Autowired	
	public SettingService(MongoTemplate template) {
		this.template = template;
	}
	
	public Receiver getReceiverById(String id) {
		try {
			Query query = Query.query(Criteria.where("id").is(new ObjectId(id)));
			return template.findOne(query, Receiver.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<Receiver> getReceiverList(Boolean enabled) {
		try {
			Query query;
			if(enabled == null) {
				query = Query.query(new Criteria());
			} else {
				query = Query.query(Criteria.where("enabled").is(enabled));
			}
			query.with(new Sort("order"));
			
			return template.find(query, Receiver.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void saveUpdateReceiver(SettingCriteriaReq req) {
		try {
			Receiver rv;
			if(StringUtils.isBlank(req.getId())) {
				rv = new Receiver();
				rv.setEnabled(true);
				rv.setCreatedDateTime(Calendar.getInstance().getTime());
				rv.setOrder(1000);
			} else {
				rv = template.findOne(Query.query(Criteria.where("id").is(req.getId())), Receiver.class);
			}
			
			rv.setReceiverName(req.getReceiverName());
			rv.setSenderName(req.getSenderName());
			
			// Pass
			rv.setPassPriceBon3(req.getPassPriceBon3());
			rv.setPassPriceBon2(req.getPassPriceBon2());
			rv.setPassPriceLang2(req.getPassPriceLang2());
			rv.setPassPriceTod(req.getPassPriceTod());
			rv.setPassPriceLoy1(req.getPassPriceLoy1());
			rv.setPassPriceLoy4(req.getPassPriceLoy4());
			rv.setPassPriceLoy5(req.getPassPriceLoy5());

			rv.setPassPerBon3(req.getPassPerBon3());
			rv.setPassPerBon2(req.getPassPerBon2());
			rv.setPassPerLang2(req.getPassPerLang2());
			rv.setPassPerTod(req.getPassPerTod());
			rv.setPassPerLoy1(req.getPassPerLoy1());
			rv.setPassPerLoy4(req.getPassPerLoy4());
			rv.setPassPerLoy5(req.getPassPerLoy5());
			
			// Sale
			rv.setSalePriceBon3(req.getSalePriceBon3());
			rv.setSalePriceBon2(req.getSalePriceBon2());
			rv.setSalePriceLang2(req.getSalePriceLang2());
			rv.setSalePriceTod(req.getSalePriceTod());
			rv.setSalePriceLoy1(req.getSalePriceLoy1());
			rv.setSalePriceLoy4(req.getSalePriceLoy4());
			rv.setSalePriceLoy5(req.getSalePriceLoy5());

			rv.setSalePerBon3(req.getSalePerBon3());
			rv.setSalePerBon2(req.getSalePerBon2());
			rv.setSalePerLang2(req.getSalePerLang2());
			rv.setSalePerTod(req.getSalePerTod());
			rv.setSalePerLoy1(req.getSalePerLoy1());
			rv.setSalePerLoy4(req.getSalePerLoy4());
			rv.setSalePerLoy5(req.getSalePerLoy5());
			
			template.save(rv);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateOrder(SettingCriteriaReq req) throws Exception {
		try {
			LOG.debug("updateOrder");
			List<Map> orderData = req.getOrderData();
			Update update;
			
			for (Map data : orderData) {
				update = new Update();
				update.set("order", Integer.parseInt(data.get("order").toString()));
				template.updateFirst(Query.query(Criteria.where("_id").is(new ObjectId(data.get("id").toString()))), update, "receiver");
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
