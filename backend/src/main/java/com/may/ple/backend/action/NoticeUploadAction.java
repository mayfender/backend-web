package com.may.ple.backend.action;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.NoticeFindCriteriaResp;
import com.may.ple.backend.criteria.ProductSearchCriteriaReq;
import com.may.ple.backend.criteria.ProductSearchCriteriaResp;
import com.may.ple.backend.service.NoticeUploadService;
import com.may.ple.backend.service.ProductService;

@Component
@Path("notice")
public class NoticeUploadAction {
	private static final Logger LOG = Logger.getLogger(NoticeUploadAction.class.getName());
	private NoticeUploadService service;
	private ProductService prodService;
	
	@Autowired
	public NoticeUploadAction(NoticeUploadService service, ProductService prodService) {
		this.service = service;
		this.prodService = prodService;
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
			req.setCurrentProduct(currentProduct);
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
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public NoticeFindCriteriaResp find(NoticeFindCriteriaReq req) {
		LOG.debug("Start");
		NoticeFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			
			if(req.getCurrentProduct() == null && req != null && req.getIsInit()) {
				LOG.debug("Find product");
				ProductSearchCriteriaReq prodReq = new ProductSearchCriteriaReq();
				prodReq.setCurrentPage(1);
				prodReq.setItemsPerPage(1000);
				prodReq.setEnabled(1);
				ProductSearchCriteriaResp findProduct = prodService.findProduct(prodReq);
				
				req.setCurrentProduct(findProduct.getProducts().get(0).getId());
				
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
	
	/*@POST
	@Path("/deleteFileTask")
	public NewTaskCriteriaResp deleteFileTask(NewTaskCriteriaReq req) {
		LOG.debug("Start");
		NewTaskCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.deleteFileTask(req.getCurrentProduct(), req.getId());
			
			resp = findAll(req);
		} catch (Exception e) {
			resp = new NewTaskCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}*/
		
}
