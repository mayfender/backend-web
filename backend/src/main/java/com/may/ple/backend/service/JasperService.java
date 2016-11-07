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
			
			List<String> ids = new ArrayList<>();
			ids.add(req.getTaskDetailId());
			
			TaskDetailViewCriteriaReq taskReq = new TaskDetailViewCriteriaReq();
			taskReq.setIds(ids);
			taskReq.setProductId(req.getProductId());
						
			TaskDetailViewCriteriaResp taskResp = taskDetailService.getTaskDetailToNotice(taskReq);
			List<Map> taskDetails = taskResp.getTaskDetails();
			taskDetails.get(0).put("address", addr);
			
			String jasperFile = FilenameUtils.removeExtension(filePath) + "/template.jasper";
			
			byte[] data = new JasperReportEngine().toPdf(jasperFile, taskDetails);	
			
			LOG.debug("End");
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String exportNotices(String prodcutId, List<String> taskDetailIds, String filePath) throws Exception {
		try {
			LOG.debug("Start");
			
			TaskDetailViewCriteriaReq taskReq = new TaskDetailViewCriteriaReq();
			taskReq.setIds(taskDetailIds);
			taskReq.setProductId(prodcutId);
			
			TaskDetailViewCriteriaResp taskResp = taskDetailService.getTaskDetailToNotice(taskReq);
			List<Map> taskDetails = taskResp.getTaskDetails();
			
			for (Map map : taskDetails) {
				map.put("address", "test");
			}
			
			String jasperFile = FilenameUtils.removeExtension(filePath) + "/template.jasper";
			
			String pdfFile = new JasperReportEngine().toPdfFile(jasperFile, taskDetails);	
			
			LOG.debug("End");
			return pdfFile;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
