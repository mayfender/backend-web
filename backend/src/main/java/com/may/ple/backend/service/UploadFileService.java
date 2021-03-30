package com.may.ple.backend.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.UploadFileCriteriaReq;
import com.may.ple.backend.criteria.UploadFileCriteriaResp;

@Service
public class UploadFileService {
	private static final Logger LOG = Logger.getLogger(UploadFileService.class.getName());
	private MongoTemplate template;

	@Autowired
	public UploadFileService(MongoTemplate template) {
		this.template = template;
	}

	public List<Map> getFiles(UploadFileCriteriaReq req) {
		try {
			UploadFileCriteriaResp resp = new UploadFileCriteriaResp();
			Criteria criteria = new Criteria();

			long totalItems = template.count(Query.query(criteria), "orderFile");
			resp.setTotalItems(totalItems);

			if(totalItems > 0) {
				Query query = Query.query(criteria).with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
				return template.find(query, Map.class, "orderFile");
			}

			return null;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public Map<String, Object> getLastPeriod() {
		try {
			Query query = new Query();
			query.with(new Sort(Sort.Direction.DESC, "periodDateTime"));
			return template.findOne(query, Map.class, "period");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
