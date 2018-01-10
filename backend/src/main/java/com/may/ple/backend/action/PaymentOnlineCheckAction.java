package com.may.ple.backend.action;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.FileCommonCriteriaResp;
import com.may.ple.backend.criteria.PaymentOnlineChkCriteriaReq;
import com.may.ple.backend.service.PaymentOnlineCheckService;
import com.may.ple.backend.service.SettingService;

@Component
@Path("paymentOnlineCheck")
public class PaymentOnlineCheckAction {
	private static final Logger LOG = Logger.getLogger(PaymentOnlineCheckAction.class.getName());
	private PaymentOnlineCheckService service;
	private SettingService settingService;
	
	@Autowired
	public PaymentOnlineCheckAction(PaymentOnlineCheckService service, SettingService settingService) {
		this.service = service;
		this.settingService = settingService;
	}
	
	@POST
	@Path("/getCheckListShow")
	@Produces(MediaType.APPLICATION_JSON)
	public FileCommonCriteriaResp getCheckListShow(PaymentOnlineChkCriteriaReq req) {
		LOG.debug("Start");
		FileCommonCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			resp = service.getCheckListShow(req);
		} catch (Exception e) {
			resp = new FileCommonCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/getCheckList")
	@Produces(MediaType.APPLICATION_JSON)
	public FileCommonCriteriaResp getCheckList(@Context HttpServletRequest requestContext, PaymentOnlineChkCriteriaReq req) {
		LOG.debug("Start");
		FileCommonCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			resp = service.getCheckList(req);
			
			settingService.setChkPayIP(requestContext.getRemoteAddr());
			LOG.info("Chkpay IP: " + settingService.getChkPayIP());
		} catch (Exception e) {
			resp = new FileCommonCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/clearStatusChkLst")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp clearStatusChkLst(PaymentOnlineChkCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.clearStatusChkLst(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateChkLst")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateChkLst(PaymentOnlineChkCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateChkLst(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getHtml")
	public FileCommonCriteriaResp getHtml(@QueryParam("id")String id, @QueryParam("productId")String productId) {
		LOG.debug("Start");
		FileCommonCriteriaResp resp;
		
		try {
			resp = service.getHtml(id, productId, true);
		} catch (Exception e) {
			resp = new FileCommonCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getHtml2Pdf")
	public Response getHtml2Pdf(@QueryParam("id")String id, @QueryParam("productId")String productId) throws Exception {
		try {
			final byte[] data = service.getHtml2Pdf(productId, id);
			
			StreamingOutput resp = new StreamingOutput() {
				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException {
					BufferedOutputStream out = null;
					ByteArrayInputStream in = null;
					try {
						out = new BufferedOutputStream(output);
						in = new ByteArrayInputStream(data);
						IOUtils.copy(in,out);						
					} catch (Exception e) {
						LOG.error(e.toString());
					} finally {
						try { if(in != null) in.close(); } catch (Exception e2) {}
						try { if(out != null) out.close(); } catch (Exception e2) {}
					}
				}
			};
			
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode("paymnetDetail.pdf"));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
}
