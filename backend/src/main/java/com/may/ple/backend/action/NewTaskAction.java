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
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.NewTaskCriteriaResp;

@Component
@Path("newTask")
public class NewTaskAction {
	private static final Logger LOG = Logger.getLogger(NewTaskAction.class.getName());
	
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		LOG.debug("Start");
		NewTaskCriteriaResp resp = null;
		int status = 200;
		
		try {
			
//			service.save(uploadedInputStream, fileDetail);
//			resp = service.findAll(1, 10);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new NewTaskCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	/*@GET
	@Path("/findAll")
	@Produces(MediaType.APPLICATION_JSON)
	public NewTaskCriteriaResp findAll(@QueryParam("currentPage")Integer currentPage, @QueryParam("itemsPerPage")Integer itemsPerPage) {
		LOG.debug("Start");
		NewTaskCriteriaResp resp = new NewTaskCriteriaResp();
		
		try {
			LOG.debug("currentPage: " + currentPage + ", itemsPerPage: " + itemsPerPage);
			
			resp = service.findAll(currentPage, itemsPerPage);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}*/
		
}
