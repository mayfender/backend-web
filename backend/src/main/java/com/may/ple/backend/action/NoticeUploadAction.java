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
import com.may.ple.backend.criteria.NoticeDownloadCriteriaResp;
import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.NoticeFindCriteriaResp;
import com.may.ple.backend.criteria.NoticeUpdateCriteriaReq;
import com.may.ple.backend.criteria.ProductSearchCriteriaReq;
import com.may.ple.backend.criteria.ProductSearchCriteriaResp;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaResp;
import com.may.ple.backend.service.NoticeUploadService;
import com.may.ple.backend.service.ProductService;
import com.may.ple.backend.service.TaskDetailService;

@Component
@Path("notice")
public class NoticeUploadAction {
	private static final Logger LOG = Logger.getLogger(NoticeUploadAction.class.getName());
	private NoticeUploadService service;
	private ProductService prodService;
	private TaskDetailService taskDetailService;
	
	@Autowired
	public NoticeUploadAction(NoticeUploadService service, ProductService prodService, TaskDetailService taskDetailService) {
		this.service = service;
		this.prodService = prodService;
		this.taskDetailService = taskDetailService;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail, 
			@FormDataParam("currentProduct") String currentProduct, @FormDataParam("templateName") String templateName) {
		LOG.debug("Start");
		NoticeFindCriteriaResp resp = null;
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
			resp = new NoticeFindCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/download")
	public Response download(NoticeFindCriteriaReq req) throws Exception {
		try {
			LOG.debug(req);
			
			LOG.debug("Get file");
			Map<String, String> map = service.getNoticeFile(req);
			String fileName = map.get("fileName");
			String filePath = map.get("filePath");
			
			LOG.debug("Get taskDetail");
			TaskDetailViewCriteriaReq taskReq = new TaskDetailViewCriteriaReq();
			taskReq.setId(req.getTaskDetailId());
			taskReq.setProductId(req.getProductId());
			TaskDetailViewCriteriaResp taskResp = taskDetailService.getTaskDetailToNotice(taskReq);
			Map<String, Object> taskDetail = taskResp.getTaskDetail();
			
			LOG.debug("Gen file");
			NoticeDownloadCriteriaResp resp = new NoticeDownloadCriteriaResp();
			resp.setFillTemplate(req.getIsFillTemplate() == null ? false : req.getIsFillTemplate());
			resp.setFilePath(filePath);
			resp.setAddress(req.getAddress());
			resp.setTaskDetail(taskDetail);
			
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
	public NoticeFindCriteriaResp find(NoticeFindCriteriaReq req) {
		LOG.debug("Start");
		NoticeFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			
			if(req.getProductId() == null && req.getIsInit() != null && req.getIsInit()) {
				LOG.debug("Find product");
				ProductSearchCriteriaReq prodReq = new ProductSearchCriteriaReq();
				prodReq.setCurrentPage(1);
				prodReq.setItemsPerPage(1000);
				prodReq.setEnabled(1);
				ProductSearchCriteriaResp findProduct = prodService.findProduct(prodReq);
				
				req.setProductId(findProduct.getProducts().get(0).getId());
				
				resp = service.find(req);				
				resp.setProducts(findProduct.getProducts());
			} else {
				resp = service.find(req);
			}
		} catch (Exception e) {
			resp = new NoticeFindCriteriaResp(1000);
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
	@Path("/deleteNoticeFile")
	public NoticeFindCriteriaResp deleteNoticeFile(NoticeFindCriteriaReq req) {
		LOG.debug("Start");
		NoticeFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.deleteFileTask(req.getProductId(), req.getId());
			
			resp = find(req);
		} catch (Exception e) {
			resp = new NoticeFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}
