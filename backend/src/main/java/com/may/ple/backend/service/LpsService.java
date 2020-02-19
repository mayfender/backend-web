package com.may.ple.backend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.bussiness.LPSWorker;
import com.may.ple.backend.criteria.LpsCriteriaReq;
import com.may.ple.backend.criteria.LpsCriteriaResp;
import com.may.ple.backend.entity.ApplicationSetting;
import com.may.ple.backend.model.DbFactory;

@Service
public class LpsService {
	private static final Logger LOG = Logger.getLogger(LpsService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	private ThreadPoolExecutor executor;
	
	@Autowired	
	public LpsService(MongoTemplate template, DbFactory dbFactory) {
		this.templateCore = template;
		this.dbFactory = dbFactory;
		executor = new ThreadPoolExecutor(10, 15, 180, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	public LpsCriteriaResp find(LpsCriteriaReq req) throws Exception {
		try {
			LpsCriteriaResp resp = new LpsCriteriaResp();
			
			Criteria criteria = Criteria.where("enabled").is(1).and("productSetting.lps.isLps").is(true);
			Query query = Query.query(criteria);
			query.fields()
			.include("_id")
			.include("productName")
			.include("productSetting.lps.mapping");
			
			List<Map> prods = templateCore.find(query, Map.class, "product");	
			MongoTemplate template;
			Map<String, Map> result = new HashMap<>();
			
			for (Map product : prods) {				
				template = dbFactory.getTemplates().get(product.get("_id").toString());
				
				executor.execute(new LPSWorker(template, product, result, req));
			}
			
			LOG.info("LPS Product " + prods.size());
			while(executor.getTaskCount() != executor.getCompletedTaskCount()){
				LOG.info("count = " + executor.getTaskCount() + ", " + executor.getCompletedTaskCount());
				Thread.sleep(2000);
				LOG.info("Active Task " + executor.getActiveCount());
			}
			LOG.info("Finished All Tasks.");
			
			ApplicationSetting appSetting = templateCore.findOne(new Query(), ApplicationSetting.class);
			resp.setLpsTel(appSetting.getLpsTel());
			resp.setFields(new ArrayList<String>(Arrays.asList(appSetting.getLpsField().split(","))));
			resp.setLpsList(result);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
