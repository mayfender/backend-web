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

import com.may.ple.backend.criteria.SptImportFingerFileCriteriaResp;
import com.may.ple.backend.service.SptImportFingerFileService;

@Component
@Path("importFingerLog")
public class SptImportFingerLogAction {
	private static final Logger LOG = Logger.getLogger(SptImportFingerLogAction.class.getName());
	private SptImportFingerFileService service;
	
	
	@Autowired
	public SptImportFingerLogAction(SptImportFingerFileService service) {
		this.service = service;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		LOG.debug("Start");
		SptImportFingerFileCriteriaResp resp = null;
		
		try {
			
			service.save(uploadedInputStream, fileDetail);
			
			resp = service.findAll(1, 10);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			return Response.status(1000).entity("Error").build();
		}
		
		LOG.debug("End");
		return Response.status(200).entity(resp).build();
	}
	
	@GET
	@Path("/findAll")
	@Produces(MediaType.APPLICATION_JSON)
	public SptImportFingerFileCriteriaResp showMemberType(@QueryParam("currentPage")Integer currentPage, @QueryParam("itemsPerPage")Integer itemsPerPage) {
		LOG.debug("Start");
		SptImportFingerFileCriteriaResp resp = new SptImportFingerFileCriteriaResp();
		
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
	}
		
}
