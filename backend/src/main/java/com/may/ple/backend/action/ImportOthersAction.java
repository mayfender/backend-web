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

import com.may.ple.backend.criteria.NewTaskCriteriaResp;
import com.may.ple.backend.service.ImportOthersService;

@Component
@Path("importOthers")
public class ImportOthersAction {
	private static final Logger LOG = Logger.getLogger(ImportOthersAction.class.getName());
	private ImportOthersService service;
	
	@Autowired
	public ImportOthersAction(ImportOthersService service) {
		this.service = service;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, 
							@FormDataParam("file") FormDataContentDisposition fileDetail, 
							@FormDataParam("productId") String productId,
							@FormDataParam("menuId") String menuId) {
		LOG.debug("Start");
		NewTaskCriteriaResp resp = null;
		int status = 200;
		
		try {
			LOG.debug("ProductID: " + productId + ", MenuID: " + menuId);
			
			//--: Save to database
			/*LOG.debug("call save");
			service.save(uploadedInputStream, fileDetail, productId);
			
			LOG.debug("Find task to show");
			NewTaskCriteriaReq req = new NewTaskCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			req.setCurrentProduct(productId);
			resp = service.findAll(req);*/
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new NewTaskCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	/*@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public NewTaskCriteriaResp find(NewTaskCriteriaReq req) {
		LOG.debug("Start");
		NewTaskCriteriaResp resp;
		
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
				
				resp = service.findAll(req);				
				resp.setProducts(findProduct.getProducts());
			} else {
				resp = service.findAll(req);
			}
		} catch (Exception e) {
			resp = new NewTaskCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/deleteFileTask")
	public NewTaskCriteriaResp deleteFileTask(NewTaskCriteriaReq req) {
		LOG.debug("Start");
		NewTaskCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.deleteFileTask(req.getCurrentProduct(), req.getId());
			
			resp = find(req);
		} catch (Exception e) {
			resp = new NewTaskCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}*/
		
}
