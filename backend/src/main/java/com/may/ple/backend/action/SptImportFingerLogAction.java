package com.may.ple.backend.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

@Component
@Path("importFingerLog")
public class SptImportFingerLogAction {
	private static final Logger LOG = Logger.getLogger(SptImportFingerLogAction.class.getName());
	/*private SptRegistrationService service;
	
	
	@Autowired
	public SptImportFingerLogAction(SptRegistrationService service) {
		this.service = service;
	}*/
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		LOG.debug("Start");
		BufferedReader reader = null;
		
		try {
			
			reader = new BufferedReader(new InputStreamReader(uploadedInputStream));
	        StringBuilder out = new StringBuilder();
	        String line;
	        
	        while ((line = reader.readLine()) != null) {
	            out.append(line);
	        }
	        System.out.println(out.toString());   //Prints the string content read from input stream
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		} finally {
			try { if(reader != null) reader.close(); } catch (IOException e) {}
		}
		
		LOG.debug("End");
		return Response.status(200).entity("Testing").build();
	}
		
}
