package com.may.ple.backend.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaResp;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.utils.XDocUtil;

@Service
public class XDocService {
	private static final Logger LOG = Logger.getLogger(XDocService.class.getName());
	private TaskDetailService taskDetailService;
	private MongoTemplate templateCenter;
	
	@Autowired	
	public XDocService(TaskDetailService taskDetailService, MongoTemplate templateCenter) {
		this.taskDetailService = taskDetailService;
		this.templateCenter = templateCenter;
	}
	
	public byte[] exportNotice(NoticeFindCriteriaReq req, String filePath, String addr, Date dateInput, String customerName) throws Exception {
		InputStream in = null;
		
		try {
			LOG.debug("Start");
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			String host = productSetting.getOpenOfficeHost();
			Integer port = productSetting.getOpenOfficePort();
			
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
			
			//--:
			byte[] data = XDocUtil.generateToPdf(filePath, detail, host, port);
			
			LOG.debug("End");
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(in != null) in.close(); } catch (Exception e2) {}
		}
	}
		
}
