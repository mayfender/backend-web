package com.may.ple.backend.service;

import static com.may.ple.backend.constant.SysFieldConstant.SYS_APPOINT_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_NEXT_TIME_DATE;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.TraceFindCriteriaReq;
import com.may.ple.backend.criteria.TraceFindCriteriaResp;
import com.may.ple.backend.criteria.TraceSaveCriteriaReq;
import com.may.ple.backend.entity.ActionCode;
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
			
			if(StringUtils.isBlank(req.getId())) {
				traceWork = new TraceWork(req.getResultText(), req.getTel(), req.getActionCode(), req.getResultCode(), req.getAppointDate(), req.getNextTimeDate());				
				traceWork.setCreatedDateTime(date);
				traceWork.setContractNo(req.getContractNo());
				traceWork.setCreatedBy(user.getId());		
				
				Update update = new Update();
				update.set(SYS_APPOINT_DATE.getName(), req.getAppointDate());
				update.set(SYS_NEXT_TIME_DATE.getName(), req.getNextTimeDate());
				template.updateFirst(Query.query(Criteria.where("_id").is(req.getTaskDetailId())), update, "newTaskDetail");
			} else {
				traceWork = template.findOne(Query.query(Criteria.where("id").is(req.getId())), TraceWork.class);
				traceWork.setResultText(req.getResultText());
				traceWork.setTel(req.getTel());
				traceWork.setActionCode(req.getActionCode());
				traceWork.setResultCode(req.getResultCode());
				traceWork.setAppointDate(req.getAppointDate());
				traceWork.setNextTimeDate(req.getNextTimeDate());
				traceWork.setUpdatedBy(user.getId());
				
				Query q = Query.query(Criteria.where("contractNo").is(traceWork.getContractNo()));
				q.with(new Sort(Sort.Direction.DESC, "createdDateTime"));
				TraceWork lastestTrace = template.findOne(q, TraceWork.class);
				
				if(lastestTrace.getId().equals(req.getId())) {
					LOG.info("Update newTaskDetail " + SYS_APPOINT_DATE.getName() + " and " + SYS_NEXT_TIME_DATE.getName() + " also.");
					
					Update update = new Update();
					update.set(SYS_APPOINT_DATE.getName(), req.getAppointDate());
					update.set(SYS_NEXT_TIME_DATE.getName(), req.getNextTimeDate());
					template.updateFirst(Query.query(Criteria.where("_id").is(req.getTaskDetailId())), update, "newTaskDetail");
				}
			}
			
			traceWork.setUpdatedDateTime(date);
			
			LOG.debug("Save");
			template.save(traceWork);
			
			template.indexOps(TraceWork.class).ensureIndex(new Index().on("createdDateTime", Direction.ASC));
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void delete(String id, String productId, String contractNo, String taskDetailId) {
		try {
			LOG.debug("Start");
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			template.remove(Query.query(Criteria.where("id").is(id)), TraceWork.class);
			
			long totalItems = template.count(Query.query(Criteria.where("contractNo").is(contractNo)), TraceWork.class);
			if(totalItems == 0) {
				Update update = new Update();
				update.set(SYS_APPOINT_DATE.getName(), null);
				update.set(SYS_NEXT_TIME_DATE.getName(), null);
				template.updateFirst(Query.query(Criteria.where("_id").is(taskDetailId)), update, "newTaskDetail");
			}
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
