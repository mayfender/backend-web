package com.may.ple.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.bussiness.jasper.JasperReportEngine;
import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaResp;

@Service
public class JasperService {
	private static final Logger LOG = Logger.getLogger(JasperService.class.getName());
	private TaskDetailService taskDetailService;
	
	@Autowired	
	public JasperService(TaskDetailService taskDetailService) {
		this.taskDetailService = taskDetailService;
	}
	
	public byte[] exportNotice(NoticeFindCriteriaReq req, String filePath, String addr) throws Exception {
		try {
			LOG.debug("Start");
			
			TaskDetailViewCriteriaReq taskReq = new TaskDetailViewCriteriaReq();
			taskReq.setId(req.getTaskDetailId());
			taskReq.setProductId(req.getProductId());
			
			List<Map<String, Object>> dataSource = new ArrayList<>();
			
			TaskDetailViewCriteriaResp taskResp = taskDetailService.getTaskDetailToNotice(taskReq);
			Map params = taskResp.getTaskDetail();
			params.put("address", addr);
			
			dataSource.add(params);
			
			String jasperFile = FilenameUtils.removeExtension(filePath) + "/template.jasper";
			
			byte[] data = new JasperReportEngine().toPdf(jasperFile, dataSource);	
			
			LOG.debug("End");
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
