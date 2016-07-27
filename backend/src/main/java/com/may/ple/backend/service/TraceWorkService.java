package com.may.ple.backend.service;

import java.util.Date;
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

import com.may.ple.backend.criteria.TraceFindCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaResp;
import com.may.ple.backend.criteria.TraceSaveCriteriaReq;
import com.may.ple.backend.entity.ActionCode;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.ResultCode;
import com.may.ple.backend.entity.TraceWork;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;

@Service
public class TraceWorkService {
	private static final Logger LOG = Logger.getLogger(TraceWorkService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	
	@Autowired	
	public TraceWorkService(MongoTemplate template, DbFactory dbFactory) {
		this.templateCore = template;
		this.dbFactory = dbFactory;
	}
	
	public TraceFindCriteriaResp find(TraceFindCriteriaReq req) throws Exception {
		try {			
			TraceFindCriteriaResp resp = new TraceFindCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			Criteria criteria = Criteria.where("contractNo").is(req.getContractNo());
			Query query = Query.query(criteria);
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			query.with(new Sort(Sort.Direction.DESC, "createdDateTime"));
			query.fields()
			.include("resultText")
			.include("tel")
			.include("actionCode")
			.include("resultCode")
			.include("appointDate")
			.include("nextTimeDate")
			.include("contractNo")
			.include("createdDateTime")
			.include("createdBy");

			LOG.debug("Get total record");
			long totalItems = template.count(Query.query(criteria), TraceWork.class);
			
			LOG.debug("Find");
			List<TraceWork> traceWorks = template.find(query, TraceWork.class);			
			
			//----
			
			LOG.debug("Get actionCode");
			List<ActionCode> actionCodes = template.findAll(ActionCode.class);
			LOG.debug("Get resultCode");
			List<ResultCode> resultCodes = template.findAll(ResultCode.class);
			LOG.debug("Get users");
			List<Users> users = templateCore.find(Query.query(Criteria.where("products").in(req.getProductId())), Users.class);
			
			LOG.debug("Start merge value");
			for (TraceWork trace : traceWorks) {
				for (ActionCode acc : actionCodes) {
					if(trace.getActionCode().equals(acc.getId())) {
						trace.setActionCodeText(acc.getCode());
						break;
					}
				}
				for (ResultCode rsc : resultCodes) {
					if(trace.getResultCode().equals(rsc.getId())) {
						trace.setResultCodeText(rsc.getCode());
						break;
					}
				}
				for (Users u : users) {
					if(trace.getCreatedBy().equals(u.getId())) {
						trace.setCreatedByText(u.getShowname());
						break;
					}
				}
			}
			LOG.debug("End merge value");
			
			resp.setTraceWorks(traceWorks);
			resp.setTotalItems(totalItems);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void save(TraceSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			TraceWork traceWork;
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			LOG.debug("Get product");
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting setting;
			
			if((setting = product.getProductSetting()) == null) {
				LOG.error("Product Setting is empty.");
				return;
			}
			
			Query query = Query.query(Criteria.where("_id").is(req.getTaskDetailId()));
			query.fields().include(setting.getContractNoColumnName());
			
			LOG.debug("Get taskdetail");
			Map taskDetails = template.findOne(query, Map.class, "newTaskDetail");
			String contractNo = String.valueOf(taskDetails.get(setting.getContractNoColumnName()));
			
			traceWork = new TraceWork(req.getResultText(), req.getTel(), req.getActionCode(), req.getResultCode(), req.getAppointDate(), req.getNextTimeDate());
			traceWork.setContractNo(contractNo);
			traceWork.setCreatedDateTime(date);
			traceWork.setUpdatedDateTime(date);
			traceWork.setCreatedBy(user.getId());				
			
			LOG.debug("Save");
			template.save(traceWork);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
