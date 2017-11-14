package com.may.ple.backend.action;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.FileCommonCriteriaResp;
import com.may.ple.backend.criteria.PaymentFindCriteriaReq;
import com.may.ple.backend.criteria.PaymentOnlineChkCriteriaReq;
import com.may.ple.backend.service.PaymentOnlineCheckService;

@Component
@Path("paymentOnlineCheck")
public class PaymentOnlineCheckAction {
	private static final Logger LOG = Logger.getLogger(PaymentOnlineCheckAction.class.getName());
	private PaymentOnlineCheckService service;
	
	@Autowired
	public PaymentOnlineCheckAction(PaymentOnlineCheckService service) {
		this.service = service;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, 
						   @FormDataParam("file") FormDataContentDisposition fileDetail, 
						   @FormDataParam("productId") String productId) {
		LOG.debug("Start");
		FileCommonCriteriaResp resp = null;
		int status = 200;
		
		try {			
			//--: Save to database
			LOG.debug("call save");
			service.save(uploadedInputStream, fileDetail, productId);
			
			LOG.debug("Find task to show");
			PaymentFindCriteriaReq req = new PaymentFindCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			req.setProductId(productId);
			resp = service.find(req);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new FileCommonCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public FileCommonCriteriaResp find(PaymentFindCriteriaReq req) {
		LOG.debug("Start");
		FileCommonCriteriaResp resp;
		
		try {
			LOG.debug(req);	
			resp = service.find(req);
		} catch (Exception e) {
			resp = new FileCommonCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/deleteFile")
	public FileCommonCriteriaResp deleteFile(PaymentFindCriteriaReq req) {
		LOG.debug("Start");
		FileCommonCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.deleteFile(req.getProductId(), req.getId());
			
			resp = service.find(req);
		} catch (Exception e) {
			resp = new FileCommonCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/addContractNo")
	public FileCommonCriteriaResp addContractNo(PaymentOnlineChkCriteriaReq req) {
		LOG.debug("Start");
		FileCommonCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.addContractNo(req);
			
			LOG.debug("Call getCheckList");
			resp = service.getCheckListShow(req);
		} catch (Exception e) {
			resp = new FileCommonCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
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
	public FileCommonCriteriaResp getCheckList(PaymentOnlineChkCriteriaReq req) {
		LOG.debug("Start");
		FileCommonCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			resp = service.getCheckList(req);
		} catch (Exception e) {
			resp = new FileCommonCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/initData")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp getProfile(@QueryParam("productId")String productId) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			service.initData(productId);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/deleteChkLstItem")
	public FileCommonCriteriaResp deleteChkLstItem(PaymentOnlineChkCriteriaReq req) {
		LOG.debug("Start");
		FileCommonCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.deleteChkLstItem(req.getProductId(), req.getId());
			
			LOG.debug("Call getCheckList");
			resp = service.getCheckListShow(req);
		} catch (Exception e) {
			resp = new FileCommonCriteriaResp(1000);
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
			resp = new FileCommonCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}
