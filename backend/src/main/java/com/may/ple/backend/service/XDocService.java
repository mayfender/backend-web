package com.may.ple.backend.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.bussiness.jasper.JasperReportEngine;
import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaResp;
import com.may.ple.backend.utils.JodConverterUtil;

import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;

@Service
public class XDocService {
	private static final Logger LOG = Logger.getLogger(XDocService.class.getName());
	private TaskDetailService taskDetailService;
	
	@Autowired	
	public XDocService(TaskDetailService taskDetailService) {
		this.taskDetailService = taskDetailService;
	}
	
	public byte[] exportNotice(NoticeFindCriteriaReq req, String filePath, String addr, Date dateInput, String customerName) throws Exception {
		InputStream in = null;
		
		try {
			LOG.debug("Start");
			
			List<String> ids = new ArrayList<>();
			ids.add(req.getTaskDetailId());
			
			TaskDetailViewCriteriaReq taskReq = new TaskDetailViewCriteriaReq();
			taskReq.setIds(ids);
			taskReq.setProductId(req.getProductId());
						
			TaskDetailViewCriteriaResp taskResp = taskDetailService.getTaskDetailToNotice(taskReq);
			List<Map> taskDetails = taskResp.getTaskDetails();
			Map detail = taskDetails.get(0);
			detail.put("address_sys", addr);
			detail.put("dateInput_sys", dateInput);
			detail.put("today_sys", Calendar.getInstance().getTime());
			
			if(!StringUtils.isBlank(customerName)) {
				detail.put("customer_name_sys", customerName);
			}
			
			// 1) Initial engine with Velocity
			in = new FileInputStream(new File(filePath));
			XDocReportRegistry registry = XDocReportRegistry.getRegistry();
			IXDocReport report = registry.loadReport(in, TemplateEngineKind.Velocity);
			
			// 2) Create context Java model
			IContext context = report.createContext();
			context.put("params", detail);
			context.put("string", "");
			
			// 3) Generate report by merging Java model with the ODT
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			report.process(context, out);
			InputStream raw = new ByteArrayInputStream(out.toByteArray());
			
			byte[] data = JodConverterUtil.toPdf(raw, org.springframework.util.StringUtils.getFilenameExtension(filePath));
			
			LOG.debug("End");
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(in != null) in.close(); } catch (Exception e2) {}
		}
	}
	
	public String exportNotices(String prodcutId, String filePath, List<String> idsAndAddr) throws Exception {
		try {
			LOG.debug("Start");
			List<String> taskDetailIds = new ArrayList<>();
			Map<String, String> paramsMap = new HashMap<>();
			String idsAddrArr[];
			
			for (String idsAddrStr : idsAndAddr) {
				idsAddrArr = idsAddrStr.split(",");
				
				if(!taskDetailIds.contains(idsAddrArr[0])) {
					taskDetailIds.add(idsAddrArr[0]);					
				}
			}
			
			TaskDetailViewCriteriaReq taskReq = new TaskDetailViewCriteriaReq();
			taskReq.setIds(taskDetailIds);
			taskReq.setProductId(prodcutId);
			
			TaskDetailViewCriteriaResp taskResp = taskDetailService.getTaskDetailToNotice(taskReq);
			List<Map> taskDetails = taskResp.getTaskDetails();
			
			List<String> checkDup = new ArrayList<>();
			List<Map> result = new ArrayList<>();
			Map<String, Object> copy;
			
			for (String idsAddrStr : idsAndAddr) {
				if(checkDup.contains(idsAddrStr)) continue;
				
				checkDup.add(idsAddrStr);
				idsAddrArr = idsAddrStr.split(",");
				
				for (Map map : taskDetails) {
					if(String.valueOf(map.get("_id")).equals(idsAddrArr[0])) {
						copy = new HashMap<>();
						copy.putAll(map);
						copy.put("address_sys", idsAddrArr[1]);
						result.add(copy);
						break;
					}
				}
			}
			
			String jasperFile = FilenameUtils.removeExtension(filePath) + "/template.jasper";
			String pdfFile = new JasperReportEngine().toPdfFile(jasperFile, result);	
			
			LOG.debug("End");
			return pdfFile;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
