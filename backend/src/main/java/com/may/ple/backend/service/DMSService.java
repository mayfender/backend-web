package com.may.ple.backend.service;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.DMSCriteriaReq;
import com.may.ple.backend.criteria.DMSCriteriaResp;

@Service
public class DMSService {
	private static final Logger LOG = Logger.getLogger(DMSService.class.getName());
	private MongoTemplate template;

	@Autowired
	public DMSService(MongoTemplate template) {
		this.template = template;
	}

	public DMSCriteriaResp getCustomers(DMSCriteriaReq req) {
		try {
			DMSCriteriaResp resp = new DMSCriteriaResp();
			Criteria criteria = new Criteria();

			if(req.getEnabled() != null) {
				criteria.and("enabled").is(req.getEnabled());
			}
			if(StringUtils.isNotBlank(req.getName())) {
				criteria.and("name").regex(Pattern.compile(req.getName(), Pattern.CASE_INSENSITIVE));
			}

			long totalItems = template.count(Query.query(criteria), "dms_customer");
			resp.setTotalItems(totalItems);

			if(totalItems > 0) {
				Query query = Query.query(criteria).with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
				resp.setCustomers(template.find(query, Map.class, "dms_customer"));
			}

			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public Map editCustomerById(String id) {
		try {
			Criteria criteria = Criteria.where("_id").is(new ObjectId(id));
			return template.findOne(Query.query(criteria), Map.class, "dms_customer");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void updateCustomer(DMSCriteriaReq req) {
		try {
			Criteria criteria = Criteria.where("_id").is(new ObjectId(req.getId()));

			Update update = new Update();
			update.set(req.getField(), req.getValue());

			template.updateFirst(Query.query(criteria), update, "dms_customer");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
