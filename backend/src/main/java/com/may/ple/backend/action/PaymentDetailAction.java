package com.may.ple.backend.action;

import java.util.Map;

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

import com.may.ple.backend.criteria.PaymentDetailCriteriaReq;
import com.may.ple.backend.criteria.PaymentDetailCriteriaResp;
import com.may.ple.backend.criteria.ToolsExcel2TextCriteriaResp;
import com.may.ple.backend.service.PaymentDetailService;
import com.may.ple.backend.service.ToolsService;

@Component
@Path("paymentDetail")
public class PaymentDetailAction {
	private static final Logger LOG = Logger.getLogger(PaymentDetailAction.class.getName());
	private PaymentDetailService service;
	private ToolsService toolService;
	
	@Autowired
	public PaymentDetailAction(PaymentDetailService service, ToolsService toolService) {
		this.service = service;
		this.toolService = toolService;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public PaymentDetailCriteriaResp find(PaymentDetailCriteriaReq req) {
		LOG.debug("Start");
		PaymentDetailCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.find(req, false, null, null);
		} catch (Exception e) {
			resp = new PaymentDetailCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/printReceipt")
	public PaymentDetailCriteriaResp printReceipt(PaymentDetailCriteriaReq req) throws Exception {
		PaymentDetailCriteriaResp resp = new PaymentDetailCriteriaResp();
		try {
			Map resultMap = service.printReceipt(req);
			resp.setFileName(String.valueOf(resultMap.get("resultFile")));
			resp.setPrintedResult((Map)resultMap.get("printedResult"));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/downloadReceipt")
	@Produces("application/pdf")
	public Response downloadBatchNotice(@QueryParam("fileName") String fileName) throws Exception {
		try {			
			ToolsExcel2TextCriteriaResp resp = new ToolsExcel2TextCriteriaResp();
			
			//-- Get file without remove that file.
			byte[] data = toolService.getFile(fileName, false);
			resp.setData(data);
			
			ResponseBuilder response = Response.ok(resp);
			response.header("\"Content-Disposition\",\"attachment; filename", new URLEncoder().encode(fileName));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
}
