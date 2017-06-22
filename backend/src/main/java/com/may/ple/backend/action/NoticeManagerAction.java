package com.may.ple.backend.action;

import java.util.Date;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.may.ple.backend.constant.NoticeFrameworkConstant;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.FindToPrintCriteriaReq;
import com.may.ple.backend.criteria.FindToPrintCriteriaResp;
import com.may.ple.backend.criteria.NoticeDownloadCriteriaResp;
import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.SaveToPrintCriteriaReq;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.service.NoticeManagerService;
import com.may.ple.backend.service.NoticeXDocUploadService;
import com.may.ple.backend.service.XDocService;

@Component
@Path("noticeManager")
public class NoticeManagerAction {
	private static final Logger LOG = Logger.getLogger(NoticeManagerAction.class.getName());
	private MongoTemplate templateCenter;
	private NoticeXDocUploadAction xdocAct;
	private NoticeUploadAction jasperAct;
	private NoticeManagerService service;
	private XDocService xdocService;
	private NoticeXDocUploadService xdocUploadService;
	
	@Autowired
	public NoticeManagerAction(MongoTemplate templateCenter, NoticeXDocUploadAction xdocAct, 
			NoticeUploadAction jasperAct, NoticeManagerService service, XDocService xdocService, NoticeXDocUploadService xdocUploadService) {
		this.xdocUploadService = xdocUploadService;
		this.templateCenter = templateCenter;
		this.xdocService = xdocService;
		this.jasperAct = jasperAct;
		this.service = service;
		this.xdocAct = xdocAct;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp find(NoticeFindCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp;
		
		try {
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			Integer noticeFramework = productSetting.getNoticeFramework();
			
			if(noticeFramework != null && NoticeFrameworkConstant.findById(noticeFramework) == NoticeFrameworkConstant.XDOC) {
				LOG.debug("Using XDoc");
				resp = xdocAct.find(req);
			} else {
				//--: Jasper is default
				LOG.debug("Using Jasper");
				resp = jasperAct.find(req);
			}
		} catch (Exception e) {
			resp = new CommonCriteriaResp() {};
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/download")
	public Response download(NoticeFindCriteriaReq req) throws Exception {
		try {
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			Integer noticeFramework = productSetting.getNoticeFramework();
			Response resp;
			
			if(noticeFramework != null && NoticeFrameworkConstant.findById(noticeFramework) == NoticeFrameworkConstant.XDOC) {
				LOG.debug("Using XDoc");
				resp = xdocAct.download(req);
			} else {
				//--: Jasper is default
				LOG.debug("Using Jasper");
				resp = jasperAct.download(req);
			}
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	@GET
	@Path("/printNotice")
	public Response printNotice(@QueryParam("productId")String productId, @QueryParam("id")String id) throws Exception {
		try {			
			LOG.debug("Call findToPrintById");
			Map noticeToPrint = service.findToPrintById(productId, id);
			
			NoticeFindCriteriaReq req = new NoticeFindCriteriaReq();
			req.setProductId(productId);
			req.setId(noticeToPrint.get("noticeId").toString());
			req.setTaskDetailId(noticeToPrint.get("taskDetailId").toString());
			req.setAddress(noticeToPrint.get("address").toString());
			req.setDateInput((Date)noticeToPrint.get("dateInput"));
			req.setCustomerName(noticeToPrint.get("customerName") != null ? noticeToPrint.get("customerName").toString() : null);
			
			LOG.debug("Get file");
			Map<String, String> map = xdocUploadService.getNoticeFile(req);
			String fileName = map.get("fileName");
			String filePath = map.get("filePath");
			
			NoticeDownloadCriteriaResp resp = new NoticeDownloadCriteriaResp();
			
			LOG.debug("Get taskDetail");
			byte data[] = xdocService.exportNotice(req, filePath, req.getAddress(), req.getDateInput(), req.getCustomerName());
			resp.setData(data);
			
			LOG.debug("Gen file");
			resp.setFillTemplate(true);
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
	@Path("/saveToPrint")
	public CommonCriteriaResp saveToPrint(SaveToPrintCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.saveToPrint(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/findToPrint")
	public CommonCriteriaResp findToPrint(FindToPrintCriteriaReq req) {
		LOG.debug("Start");
		FindToPrintCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			resp = service.findToPrint(req);
		} catch (Exception e) {
			resp = new FindToPrintCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/deleteToPrint")
	public CommonCriteriaResp deleteToPrint(FindToPrintCriteriaReq req) {
		LOG.debug("Start");
		FindToPrintCriteriaResp resp = null;
		
		try {
			LOG.debug(req);
			service.deleteToPrint(req);
			
			LOG.debug("Call findToPrint");
			resp = service.findToPrint(req);
		} catch (Exception e) {
			resp = new FindToPrintCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}