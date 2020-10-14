package com.may.ple.backend.service;

import java.util.List;

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
import com.may.ple.backend.criteria.SendRoundCriteriaReq;
import com.may.ple.backend.entity.SendRound;
import com.may.ple.backend.model.DbFactory;

@Service
public class SendRoundService {
	private static final Logger LOG = Logger.getLogger(SendRoundService.class.getName());
	private DbFactory dbFactory;

	@Autowired
	public SendRoundService(DbFactory dbFactory) {
		this.dbFactory = dbFactory;
	}

	public List<SendRound> getDataList(Boolean enabled, String dealerId) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);

			Query query;
			if(enabled == null) {
				query = Query.query(new Criteria());
			} else {
				query = Query.query(Criteria.where("enabled").is(enabled));
			}
			query.with(new Sort("limitedTime"));

			return dealerTemp.find(query, SendRound.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void saveUpdate(SendRoundCriteriaReq req) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());

			SendRound sr;
			if(StringUtils.isBlank(req.getId())) {
				sr = new SendRound();
				sr.setEnabled(true);
				sr.setCreatedDateTime(Calendar.getInstance().getTime());
				sr.setOrder(1000);
			} else {
				sr = dealerTemp.findOne(Query.query(Criteria.where("id").is(req.getId())), SendRound.class);
			}

			Calendar limitedTime = Calendar.getInstance();
			limitedTime.setTime(req.getLimitedTime());
			limitedTime.set(Calendar.DATE, 1);
			limitedTime.set(Calendar.MONTH, 0);
			limitedTime.set(Calendar.YEAR, 2020);

			sr.setLimitedTime(limitedTime.getTime());
			sr.setName(req.getName());

			dealerTemp.save(sr);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void statusToggle(SendRoundCriteriaReq req) throws Exception {
		try {
			LOG.debug("updateOrder");
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());
			Update update = new Update();
			update.set("enabled", req.getEnabled() ? false : true);
			dealerTemp.updateFirst(Query.query(Criteria.where("id").is(new ObjectId(req.getId()))), update, SendRound.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
