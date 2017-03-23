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

import com.may.ple.backend.criteria.ProgramFileFindCriteriaReq;
import com.may.ple.backend.criteria.ProgramFileFindCriteriaResp;
import com.may.ple.backend.service.ProgramService;

@Component
@Path("program")
public class ProgramAction {
	private static final Logger LOG = Logger.getLogger(ProgramAction.class.getName());
	private ProgramService service;
	
	@Autowired
	public ProgramAction(ProgramService service) {
		this.service = service;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		LOG.debug("Start");
		ProgramFileFindCriteriaResp resp = null;
		int status = 200;
		
		try {
			
			//--: Save to database
			LOG.debug("call save");
			service.save(uploadedInputStream, fileDetail);
			
			LOG.debug("Find task to show");
			ProgramFileFindCriteriaReq req = new ProgramFileFindCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			resp = service.findAll(req);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new ProgramFileFindCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/findAll")
	@Produces(MediaType.APPLICATION_JSON)
	public ProgramFileFindCriteriaResp findAll(ProgramFileFindCriteriaReq req) {
		LOG.debug("Start");
		ProgramFileFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.findAll(req);
		} catch (Exception e) {
			resp = new ProgramFileFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	
	/*@POST
	@Path("/download")
	public Response download(PaymentFindCriteriaReq req) throws Exception {
		try {
			LOG.debug(req);
			
			boolean isFillTemplate = req.getIsFillTemplate() == null ? false : req.getIsFillTemplate();
			
			LOG.debug("Get file");
			Map<String, String> map = service.getFile(req);
			String fileName = map.get("fileName");
			String filePath = map.get("filePath");
			
			LOG.debug("Gen file");
			NoticeDownloadCriteriaResp resp = new NoticeDownloadCriteriaResp();
			resp.setFillTemplate(isFillTemplate);
			resp.setFilePath(filePath);
			
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(fileName));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}*/
	
}