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

import com.may.ple.backend.criteria.NewTaskCriteriaReq;
import com.may.ple.backend.criteria.NewTaskCriteriaResp;
import com.may.ple.backend.service.NewTaskService;

@Component
@Path("newTask")
public class NewTaskAction {
	private static final Logger LOG = Logger.getLogger(NewTaskAction.class.getName());
	private NewTaskService service;
	
	@Autowired
	public NewTaskAction(NewTaskService service) {
		this.service = service;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("currentProduct") String currentProduct) {
		LOG.debug("Start");
		NewTaskCriteriaResp resp = null;
		int status = 200;
		
		try {
			
			LOG.debug(currentProduct);
			
			service.save(uploadedInputStream, fileDetail, currentProduct);
			
			NewTaskCriteriaReq req = new NewTaskCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			req.setCurrentProduct(currentProduct);
			resp = service.findAll(req);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new NewTaskCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/findAll")
	@Produces(MediaType.APPLICATION_JSON)
	public NewTaskCriteriaResp findAll(NewTaskCriteriaReq req) {
		LOG.debug("Start");
		NewTaskCriteriaResp resp;
		
		try {
			LOG.debug(req);
			
			resp = service.findAll(req);
			
		} catch (Exception e) {
			resp = new NewTaskCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
		
}
