package com.may.ple.backend.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.UserLogCriteriaReq;
import com.may.ple.backend.criteria.UserLogCriteriaResp;
import com.may.ple.backend.model.DbFactory;

@Service
public class UserLogService {
	private static final Logger LOG = Logger.getLogger(UserLogService.class.getName());
	private MongoTemplate templateCenter;
	private DbFactory dbFactory;

	@Autowired
	public UserLogService(MongoTemplate templateCenter, DbFactory dbFactory) {
		this.templateCenter = templateCenter;
		this.dbFactory = dbFactory;
	}

	public UserLogCriteriaResp getLog(UserLogCriteriaReq req) {
		try {
			UserLogCriteriaResp resp = new UserLogCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			/*LocalDate date = req.getDateLog().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
			Date start = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());

			LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
			Date end = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());*/

			//------
			Criteria criteria = Criteria.where("type").is(1);
			Query query = Query.query(criteria);
			query.fields().include("mapping");
			Map dataMapping = templateCenter.findOne(query, Map.class, "dataMapping");
			dataMapping = (Map)dataMapping.get("mapping");

			Date dateFrom, dateTo;
			if(req.getDateFrom() == null  || req.getDateTo() == null) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				dateFrom = cal.getTime();

				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				dateTo = cal.getTime();
			} else {
				dateFrom = req.getDateFrom();
				dateTo = req.getDateTo();
			}

			criteria = Criteria.where("createdDateTime").gte(dateFrom).lte(dateTo);

			//-----
			if(StringUtils.isNotBlank(req.getUserId())) {
				criteria.and("userId").is(new ObjectId(req.getUserId()));
			} else if(req.getUserGroup() != null) {
				criteria.and("userGroup").is(req.getUserGroup());
			} else {
				List<Integer> userGroup = new ArrayList<>();
				userGroup.add(1);userGroup.add(2);userGroup.add(3);userGroup.add(4);
				criteria.and("userGroup").in(userGroup);
			}

			List actionNameList = template.getCollection("userLog").distinct("actionName", Query.query(criteria).getQueryObject());
			actionNameList = actionDescMapping(actionNameList, dataMapping);
			resp.setActionList(actionNameList);

			//------
			if(StringUtils.isNotBlank(req.getActionName())) {
				criteria.and("actionName").is(req.getActionName());
			}

			//---:
			resp.setTotalItems(template.count(Query.query(criteria), "userLog"));

			//---:
			query = Query.query(criteria);
			query.with(new Sort(Sort.Direction.DESC, "createdDateTime"));
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			List<Map> userLogs = template.find(query, Map.class, "userLog");
			if(userLogs.size() == 0) return resp;

			userLogs = actionDescMapping(userLogs, dataMapping);
			resp.setLogs(userLogs);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private List actionDescMapping(List acts, Map dataMapping) {
		List result = new ArrayList<>();
		Object desc;
		Map data;

		for (Object obj : acts) {
			if(obj instanceof Map) {
				data = (Map)obj;
				desc = dataMapping.get(data.get("actionName").toString());
				data.put("actionDesc", desc == null ? data.get("actionName").toString() : desc.toString());
				result.add(data);
			} else if(obj instanceof String) {
				desc = dataMapping.get(obj.toString());
				data = new HashMap<>();
				data.put("actionName", obj.toString());
				data.put("actionDesc", desc == null ? obj.toString() : desc.toString());
				result.add(data);
			}
		}
		return result;
	}

}
