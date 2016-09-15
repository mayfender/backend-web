package com.may.ple.backend.service;

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
	
	public byte[] exportNotice(NoticeFindCriteriaReq req, String filePath) throws Exception {
		try {
			LOG.debug("Start");
			
			TaskDetailViewCriteriaReq taskReq = new TaskDetailViewCriteriaReq();
			taskReq.setId(req.getTaskDetailId());
			taskReq.setProductId(req.getProductId());
			
			TaskDetailViewCriteriaResp taskResp = taskDetailService.getTaskDetailToNotice(taskReq);
			Map params = taskResp.getTaskDetail();
			
			String jasperFile = FilenameUtils.removeExtension(filePath) + "/template.jasper";
			
			byte[] data = new JasperReportEngine().toPdf(jasperFile, params);	
			
			LOG.debug("End");
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
