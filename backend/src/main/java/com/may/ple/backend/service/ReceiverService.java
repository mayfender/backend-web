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

			rv.setPriceBon3(req.getPriceBon3());
			rv.setPriceBon2(req.getPriceBon2());
			rv.setPriceLang2(req.getPriceLang2());
			rv.setPriceTod(req.getPriceTod());
			rv.setPriceLoy(req.getPriceLoy());
			rv.setPricePare4(req.getPricePare4());
			rv.setPricePare5(req.getPricePare5());
			rv.setPriceRunBon(req.getPriceRunBon());
			rv.setPriceRunLang(req.getPriceRunLang());

			rv.setPercentBon3(req.getPercentBon3());
			rv.setPercentBon2(req.getPercentBon2());
			rv.setPercentLang2(req.getPercentLang2());
			rv.setPercentTod(req.getPercentTod());
			rv.setPercentLoy(req.getPercentLoy());
			rv.setPercentPare4(req.getPercentPare4());
			rv.setPercentPare5(req.getPercentPare5());
			rv.setPercentRunBon(req.getPercentRunBon());
			rv.setPercentRunLang(req.getPercentRunLang());

			dealerTemp.save(rv);
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
