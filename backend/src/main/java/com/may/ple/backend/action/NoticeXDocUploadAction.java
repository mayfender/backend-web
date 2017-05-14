package com.may.ple.backend.action;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.Consumes;
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
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.BatchNoticeFindCriteriaResp;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.NoticeDownloadCriteriaResp;
import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.NoticeUpdateCriteriaReq;
import com.may.ple.backend.criteria.NoticeXDocFindCriteriaResp;
import com.may.ple.backend.criteria.ToolsExcel2TextCriteriaResp;
import com.may.ple.backend.criteria.TraceResultImportFindCriteriaReq;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.service.NoticeXDocUploadService;
import com.may.ple.backend.service.ToolsService;
import com.may.ple.backend.service.XDocService;
import com.may.ple.backend.utils.FileUtil;

@Component
@Path("noticeXDoc")
public class NoticeXDocUploadAction {
	private static final Logger LOG = Logger.getLogger(NoticeXDocUploadAction.class.getName());
	private NoticeXDocUploadService service;
	private XDocService xdocService;
	private ToolsService toolService;
	
	@Autowired
	public NoticeXDocUploadAction(NoticeXDocUploadService service, XDocService xdocService, ToolsService toolService) {
		this.service = service;
		this.xdocService = xdocService;
		this.toolService = toolService;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail, 
			@FormDataParam("currentProduct") String currentProduct, @FormDataParam("templateName") String templateName) {
		LOG.debug("Start");
		NoticeXDocFindCriteriaResp resp = null;
		int status = 200;
		
		try {
			LOG.debug(currentProduct);
			
			//--: Save to database
			LOG.debug("call save");
			service.save(uploadedInputStream, fileDetail, currentProduct, templateName);
			
			LOG.debug("Find task to show");
			NoticeFindCriteriaReq req = new NoticeFindCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			req.setProductId(currentProduct);
			resp = service.find(req);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new NoticeXDocFindCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/findBatchNotice")
	@Produces(MediaType.APPLICATION_JSON)
	public BatchNoticeFindCriteriaResp findBatchNotice(TraceResultImportFindCriteriaReq req) {
		LOG.debug("Start");
		BatchNoticeFindCriteriaResp resp;
		
		try {
			LOG.debug(req);	
			resp = service.findBatchNotice(req);
		} catch (Exception e) {
			resp = new BatchNoticeFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/uploadBatchNotice")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadBatchNotice(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail, 
										@FormDataParam("productId") String productId) {
		LOG.debug("Start");
		BatchNoticeFindCriteriaResp resp = new BatchNoticeFindCriteriaResp();
		int status = 200;
		
		try {
			LOG.debug("Get Filename");
			Date now = Calendar.getInstance().getTime();
			FileDetail fd = FileUtil.getFileName2(fileDetail, now);
			
			String fileName = service.uploadBatchNotice(uploadedInputStream, fileDetail, fd, productId);				
			
			LOG.debug("Find batch");
			TraceResultImportFindCriteriaReq reqBatch = new TraceResultImportFindCriteriaReq();
			reqBatch.setCurrentPage(1);
			reqBatch.setItemsPerPage(10);
			reqBatch.setProductId(productId);
			resp = service.findBatchNotice(reqBatch);
			resp.setFileName(fileName);
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp.setStatusCode(1000);
			status = 1000;
		}		
		
		return Response.status(status).entity(resp).build();
	}
	
	@GET
	@Path("/downloadBatchNotice")
	public Response downloadBatchNotice(@QueryParam("fileName") String fileName) throws Exception {
		try {			
			ToolsExcel2TextCriteriaResp resp = new ToolsExcel2TextCriteriaResp();
			
			byte[] data = toolService.getFile(fileName);
			resp.setData(data);
			
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(fileName));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	@POST
	@Path("/deleteBatchNoticeFile")
	public BatchNoticeFindCriteriaResp deleteBatchNoticeFile(TraceResultImportFindCriteriaReq req) {
		LOG.debug("Start");
		BatchNoticeFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.deleteBatchNoticeFile(req.getProductId(), req.getId());
			
			resp = service.findBatchNotice(req);
		} catch (Exception e) {
			resp = new BatchNoticeFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/download")
	public Response download(NoticeFindCriteriaReq req) throws Exception {
		try {
			LOG.debug(req);
			
			boolean isFillTemplate = req.getIsFillTemplate() == null ? false : req.getIsFillTemplate();
			
			LOG.debug("Get file");
			Map<String, String> map = service.getNoticeFile(req);
			String fileName = map.get("fileName");
			String filePath = map.get("filePath");
			
			NoticeDownloadCriteriaResp resp = new NoticeDownloadCriteriaResp();
			
			if(isFillTemplate) {
				LOG.debug("Get taskDetail");
				byte data[] = xdocService.exportNotice(req, filePath, req.getAddress(), req.getDateInput(), req.getCustomerName());
				resp.setData(data);
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
	public NoticeXDocFindCriteriaResp find(NoticeFindCriteriaReq req) {
		LOG.debug("Start");
		NoticeXDocFindCriteriaResp resp;
		
		try {
			LOG.debug(req);	
			resp = service.find(req);
		} catch (Exception e) {
			resp = new NoticeXDocFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateTemplateName")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateTemplateName(NoticeUpdateCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateTemplateName(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateEnabled")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateEnabled(NoticeUpdateCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateEnabled(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateDateInput")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateDateInput(NoticeUpdateCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateDateInput(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	
	@POST
	@Path("/deleteNoticeFile")
	public NoticeXDocFindCriteriaResp deleteNoticeFile(NoticeFindCriteriaReq req) {
		LOG.debug("Start");
		NoticeXDocFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.deleteNoticeFile(req.getProductId(), req.getId());
			
			resp = find(req);
		} catch (Exception e) {
			resp = new NoticeXDocFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}
