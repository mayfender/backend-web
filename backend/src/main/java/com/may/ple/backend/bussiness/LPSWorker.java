package com.may.ple.backend.bussiness;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_IS_ACTIVE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;

import com.google.common.base.Splitter;
import com.may.ple.backend.criteria.LpsCriteriaReq;

public class LPSWorker implements Runnable {
	private static final Logger LOG = Logger.getLogger(LPSWorker.class.getName());
	private Map<String, Map> result;
	private MongoTemplate template;
	private LpsCriteriaReq req;
	private Map product;
	
	public LPSWorker(MongoTemplate template, Map product,  Map<String, Map> result, LpsCriteriaReq req) {
		this.template = template;
		this.product = product;
		this.result = result;
		this.req = req;
	}
	
	@Override
	public void run() {
		try {
			String productName = product.get("productName").toString();
			LOG.info("Start Product " + productName);
			
			Map lps = (Map)((Map)product.get("productSetting")).get("lps");
			String mapping = lps.get("mapping").toString();
			
			Map<String, String> splited = Splitter.on(',').trimResults().withKeyValueSeparator(
						Splitter.on('=')
		            	.limit(2)
		            	.trimResults()
					).split(mapping);
			
			List<Map> data = findImpl(template, req, splited);
			
			if(data.size() > 0) {
				Map val = new HashMap<>();
				val.put("data", data);
				val.put("mapping", splited);
				
				result.put(productName, val);
			}
			
			LOG.info("End product " + productName + ", size " + data.size());
		} catch (Exception e) {
			LOG.error(e.toString());
		}
	}
	
	private List<Map> findImpl(MongoTemplate template, LpsCriteriaReq req, Map<String, String> splited) {
		try {
			Criteria criteria = Criteria.where(SYS_IS_ACTIVE.getName() + ".status").is(true).and("lps_isActive").is(1);
			
			if(StringUtils.isNoneBlank(req.getLpsNumber()) && StringUtils.isNoneBlank(req.getLpsGroup())) {				
				criteria.and("lps_number").is(req.getLpsNumber()).and("lps_group").regex(Pattern.compile("^" + req.getLpsGroup(), Pattern.CASE_INSENSITIVE));
			} else if(StringUtils.isNoneBlank(req.getLpsNumber())) {
				criteria.and("lps_number").is(req.getLpsNumber());
			} else if(StringUtils.isNoneBlank(req.getLpsGroup())) {
				criteria.and("lps_group").regex(Pattern.compile("^" + req.getLpsGroup(), Pattern.CASE_INSENSITIVE));
			} else {
				LOG.warn("Criteria is empty.");
				return new ArrayList();
			}
			
			Query query = Query.query(criteria);
			query.limit(20);
			Field fields = query.fields();
			
			for (Map.Entry<String, String> entry : splited.entrySet()) {
				fields.include(entry.getValue());
			}
			
			return template.find(query, Map.class, NEW_TASK_DETAIL.getName());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
