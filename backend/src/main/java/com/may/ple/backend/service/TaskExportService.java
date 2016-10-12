package com.may.ple.backend.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.TaskDetailCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailCriteriaResp;

@Service
public class TaskExportService {
	private static final Logger LOG = Logger.getLogger(TaskExportService.class.getName());
	private MongoTemplate template;
	private TaskDetailService taskService;
	
	@Autowired	
	public TaskExportService(MongoTemplate template, TaskDetailService taskService) {
		this.template = template;
		this.taskService = taskService;
	}
	
	public byte[] export(TaskDetailCriteriaReq req) throws Exception {
		try {			

			TaskDetailCriteriaResp find = taskService.find(req);
			List<Map> taskDetails = find.getTaskDetails();
			
			return null;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
