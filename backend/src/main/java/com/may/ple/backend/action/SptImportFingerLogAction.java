package com.may.ple.backend.action;

import java.io.BufferedReader;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		LOG.debug("Start");
		
		try {
			
			service.save(uploadedInputStream, fileDetail);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			return Response.status(1000).entity("Error").build();
		}
		
		LOG.debug("End");
		return Response.status(200).entity("Success").build();
	}
		
}
