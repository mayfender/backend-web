package com.may.ple.backend.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import com.may.ple.backend.criteria.PriceListCriteriaReq;
import com.may.ple.backend.criteria.ReceiverCriteriaReq;
import com.may.ple.backend.entity.PriceList;
import com.may.ple.backend.entity.Receiver;
import com.may.ple.backend.model.DbFactory;
import com.mongodb.BasicDBObject;

@Service
public class ReceiverService {
	private static final Logger LOG = Logger.getLogger(ReceiverService.class.getName());
	private MongoTemplate template;
	private DbFactory dbFactory;

	@Autowired
	public ReceiverService(MongoTemplate template, DbFactory dbFactory) {
		this.template = template;
		this.dbFactory = dbFactory;
	}

	public Receiver getReceiverById(String id, String dealerId) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);
			Query query = Query.query(Criteria.where("id").is(new ObjectId(id)));
			return dealerTemp.findOne(query, Receiver.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List<Receiver> getReceiverList(Boolean enabled, String dealerId) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);

			Query query;
			if(enabled == null) {
				query = Query.query(new Criteria());
			} else {
				query = Query.query(Criteria.where("enabled").is(enabled));
			}
			query.with(new Sort("order"));

			return dealerTemp.find(query, Receiver.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void saveUpdateReceiver(ReceiverCriteriaReq req) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());

			Receiver rv;
			if(StringUtils.isBlank(req.getId())) {
				rv = new Receiver();
				rv.setEnabled(true);
				rv.setIsCuttingOff(false);
				rv.setCreatedDateTime(Calendar.getInstance().getTime());
				rv.setOrder(1000);
			} else {
				rv = dealerTemp.findOne(Query.query(Criteria.where("id").is(req.getId())), Receiver.class);
			}

			rv.setReceiverName(req.getReceiverName());
			rv.setSenderName(req.getSenderName());
			rv.setPriceListId(StringUtils.isBlank(req.getPriceListId()) ? null : new ObjectId(req.getPriceListId()));

			dealerTemp.save(rv);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void updateOrder(ReceiverCriteriaReq req) throws Exception {
		try {
			LOG.debug("updateOrder");
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());
			List<Map> orderData = req.getOrderData();
			Update update;

			for (Map data : orderData) {
				update = new Update();
				update.set("order", Integer.parseInt(data.get("order").toString()));
				dealerTemp.updateFirst(Query.query(Criteria.where("_id").is(new ObjectId(data.get("id").toString()))), update, "receiver");
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void statusToggle(ReceiverCriteriaReq req) throws Exception {
		try {
			LOG.debug("updateOrder");
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());
			Update update = new Update();
			update.set("enabled", req.getEnabled() ? false : true);
			dealerTemp.updateFirst(Query.query(Criteria.where("id").is(new ObjectId(req.getId()))), update, Receiver.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void cutOffToggle(ReceiverCriteriaReq req) throws Exception {
		try {
			LOG.debug("updateOrder");
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());
			Update update = new Update();
			update.set("isCuttingOff", req.getIsCuttingOff() != null && req.getIsCuttingOff() ? false : true);
			dealerTemp.updateFirst(Query.query(Criteria.where("id").is(new ObjectId(req.getId()))), update, Receiver.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List<PriceList> getPriceList(Boolean enabled, String dealerId) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);

			Query query;
			if(enabled == null) {
				query = Query.query(new Criteria());
			} else {
				query = Query.query(Criteria.where("enabled").is(enabled));
			}
			query.with(new Sort("order"));

			return dealerTemp.find(query, PriceList.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void saveUpdatePriceList(PriceListCriteriaReq req) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());

			PriceList rv;
			if(StringUtils.isBlank(req.getId())) {
				rv = new PriceList();
				rv.setEnabled(true);
				rv.setCreatedDateTime(Calendar.getInstance().getTime());
				rv.setOrder(1000);
			} else {
				rv = dealerTemp.findOne(Query.query(Criteria.where("id").is(req.getId())), PriceList.class);
			}

			rv.setPriceListName(req.getPriceListName());
			dealerTemp.save(rv);

			//---:
			BasicDBObject data = new BasicDBObject();
			data.put("priceBon3", req.getPriceBon3());
			data.put("priceBon2", req.getPriceBon2());
			data.put("priceLang2", req.getPriceLang2());
			data.put("priceTod", req.getPriceTod());
			data.put("priceLoy", req.getPriceLoy());
			data.put("pricePare4", req.getPricePare4());
			data.put("pricePare5", req.getPricePare5());
			data.put("priceRunBon", req.getPriceRunBon());
			data.put("priceRunLang", req.getPriceRunLang());
			data.put("pricePugBon", req.getPricePugBon());
			data.put("pricePugLang", req.getPricePugLang());

			data.put("percentBon3", req.getPercentBon3());
			data.put("percentBon2", req.getPercentBon2());
			data.put("percentLang2", req.getPercentLang2());
			data.put("percentTod", req.getPercentTod());
			data.put("percentLoy", req.getPercentLoy());
			data.put("percentPare4", req.getPercentPare4());
			data.put("percentPare5", req.getPercentPare5());
			data.put("percentRunBon", req.getPercentRunBon());
			data.put("percentRunLang", req.getPercentRunLang());
			data.put("percentPugBon", req.getPercentPugBon());
			data.put("percentPugLang", req.getPercentPugLang());

			Update update = new Update();
			update.set("priceData." + req.getSendRoundId(), data);
			dealerTemp.updateFirst(Query.query(Criteria.where("_id").is(new ObjectId(rv.getId()))), update, "priceList");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void priceListStatusToggle(PriceListCriteriaReq req) throws Exception {
		try {
			LOG.debug("updateOrder");
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());
			Update update = new Update();
			update.set("enabled", req.getEnabled() ? false : true);
			dealerTemp.updateFirst(Query.query(Criteria.where("id").is(new ObjectId(req.getId()))), update, PriceList.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void updatePriceListOrder(ReceiverCriteriaReq req) throws Exception {
		try {
			LOG.debug("updateOrder");
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());
			List<Map> orderData = req.getOrderData();
			Update update;

			for (Map data : orderData) {
				update = new Update();
				update.set("order", Integer.parseInt(data.get("order").toString()));
				dealerTemp.updateFirst(Query.query(Criteria.where("_id").is(new ObjectId(data.get("id").toString()))), update, "priceList");
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
