package com.may.ple.backend.action;

import java.util.Calendar;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.catalina.util.URLEncoder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.SmsCriteriaReq;
import com.may.ple.backend.criteria.SmsCriteriaResp;
import com.may.ple.backend.criteria.SmsReportCriteriaResp;
import com.may.ple.backend.service.SmsService;

@Component
@Path("sms")
public class SmsAction {
	private static final Logger LOG = Logger.getLogger(SmsAction.class.getName());
	private SmsService service;
	
	@Autowired
	public SmsAction(SmsService service) {
		this.service = service;
	}
	
	@POST
	@Path("/save")
	public CommonCriteriaResp save(SmsCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new SmsCriteriaResp();
		
		try {
			LOG.debug(req);
			service.save(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/get")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp get(SmsCriteriaReq req) {
		LOG.debug("Start");
		SmsCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			resp = service.get(req, null);
			
		} catch (Exception e) {
			resp = new SmsCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/remove")
	public CommonCriteriaResp remove(SmsCriteriaReq req) {
		LOG.debug("Start");
		SmsCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			service.remove(req);
			resp = service.get(req, null);
		} catch (Exception e) {
			resp = new SmsCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/download")
	public Response download(SmsCriteriaReq req) throws Exception {
		try {
			LOG.debug(req);
			
			LOG.debug("Get file");
			String templateFile = service.getTemplatePath();
			String fileName = "Report_" + String.format("%1$tH%1$tM%1$tS.xlsx", Calendar.getInstance().getTime());
			
			SmsReportCriteriaResp resp = new SmsReportCriteriaResp();
				
				//--: Set null to get all
			resp.setFilePath(templateFile);
			resp.setTraceReq(req);
			resp.setTraceService(service);
			
			LOG.debug("Gen file");
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(fileName));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	@POST
	@Path("/sendSms")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp sendSms(SmsCriteriaReq req) {
		LOG.debug("Start");
		SmsCriteriaResp resp = new SmsCriteriaResp();
		
		try {
			
			LOG.debug(req);
			service.sendSms(req);
			
		} catch (Exception e) {
			resp = new SmsCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}