package com.may.ple.backend.action;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.catalina.util.URLEncoder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.constant.TPLTypeConstant;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.EngTplCriteriaReq;
import com.may.ple.backend.criteria.SmsCriteriaReq;
import com.may.ple.backend.criteria.SmsCriteriaResp;
import com.may.ple.backend.service.EngTplService;
import com.may.ple.backend.service.SmsService;

@Component
@Path("sms")
public class SmsAction {
	private static final Logger LOG = Logger.getLogger(SmsAction.class.getName());
	private SmsService service;
	private EngTplService engService;
	
	@Autowired
	public SmsAction(SmsService service, EngTplService engService) {
		this.service = service;
		this.engService = engService;
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
			EngTplCriteriaReq engReq = new EngTplCriteriaReq();
			engReq.setProductId(req.getProductId());
			engReq.setType(TPLTypeConstant.SMS.getId());
			String filePath = engService.getFile(engReq);
			
			ResponseBuilder response = Response.ok(service.report(req, filePath));
			response.header("fileName", new URLEncoder().encode(new File(filePath).getName()));
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
	
	@GET
	@Path("/getSmsSentStatus")
	public CommonCriteriaResp getSmsSentStatus(@QueryParam("productId") String productId) {
		LOG.debug("Start");
		SmsCriteriaResp resp = new SmsCriteriaResp();
		
		try {
			resp.setMap(service.getSmsSentStatus(productId));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}