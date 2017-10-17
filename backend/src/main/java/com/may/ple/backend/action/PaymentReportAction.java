package com.may.ple.backend.action;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.catalina.util.URLEncoder;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.PaymentDetailCriteriaReq;
import com.may.ple.backend.criteria.PaymentReportCriteriaResp;
import com.may.ple.backend.criteria.PaymentReportFindCriteriaResp;
import com.may.ple.backend.criteria.TraceResultReportFindCriteriaReq;
import com.may.ple.backend.service.PaymentDetailService;
import com.may.ple.backend.service.PaymentReportService;

@Component
@Path("paymentReport")
public class PaymentReportAction {
	private static final Logger LOG = Logger.getLogger(PaymentReportAction.class.getName());
	private PaymentReportService service;
	private PaymentDetailService payemtnDetailService;
	private UserAction userAct;
	
	@Autowired
	public PaymentReportAction(PaymentReportService service, PaymentDetailService payemtnDetailService, UserAction userAct) {
		this.service = service;
		this.payemtnDetailService = payemtnDetailService;
		this.userAct = userAct;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail, 
			@FormDataParam("currentProduct") String productId) {
		LOG.debug("Start");
		PaymentReportFindCriteriaResp resp = null;
		int status = 200;
		
		try {
			LOG.debug(productId);
			
			//--: Save to database
			LOG.debug("call save");
			service.save(uploadedInputStream, fileDetail, productId);
			
			LOG.debug("Find task to show");
			TraceResultReportFindCriteriaReq req = new TraceResultReportFindCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			req.setProductId(productId);
			resp = service.find(req);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new PaymentReportFindCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/download")
	public Response download(PaymentDetailCriteriaReq req) throws Exception {
		try {
			LOG.debug(req);
			
			boolean isFillTemplate = req.getIsFillTemplate() == null ? false : req.getIsFillTemplate();
			
			LOG.debug("Get file");
			Map<String, String> map = service.getFile(req);
			String fileName = map.get("fileName");
			String filePath = map.get("filePath");
			
			PaymentReportCriteriaResp resp = new PaymentReportCriteriaResp();
			
			if(isFillTemplate) {
				LOG.debug("Get trace");
				
				//--: Set null to get all
				req.setCurrentPage(null);
				
				resp.setTraceReq(req);
				resp.setTraceService(payemtnDetailService);
				resp.setUserAct(userAct);
			}
			
			LOG.debug("Gen file");
			
			resp.setFillTemplate(isFillTemplate);
			resp.setFilePath(filePath);
			
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(fileName));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp find(TraceResultReportFindCriteriaReq req) {
		LOG.debug("Start");
		PaymentReportFindCriteriaResp resp;
		
		try {
			LOG.debug(req);	
			resp = service.find(req);
		} catch (Exception e) {
			resp = new PaymentReportFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/deleteFile")
	public CommonCriteriaResp deleteNoticeFile(TraceResultReportFindCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.deleteFile(req.getProductId(), req.getId());
			
			resp = find(req);
		} catch (Exception e) {
			resp = new CommonCriteriaResp(1000){};
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}
