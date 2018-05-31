package com.may.ple.backend.action;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.DocumentFindCriteriaReq;
import com.may.ple.backend.criteria.DocumentFindCriteriaResp;
import com.may.ple.backend.criteria.ToolsUploadCriteriaResp;
import com.may.ple.backend.service.DocumentService;

@Component
@Path("document")
public class DocumentAction {
	private static final Logger LOG = Logger.getLogger(DocumentAction.class.getName());
	private DocumentService service;
	
	@Autowired
	public DocumentAction(DocumentService service) {
		this.service = service;
	}
	
	@POST
	@Path("/uploadDoc")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadDoc(@FormDataParam("file") InputStream uploadedInputStream, 
							  @FormDataParam("file") FormDataContentDisposition fileDetail,
							  @FormDataParam("productId") String productId,
							  @FormDataParam("contractNo") String contractNo,
							  @FormDataParam("type") Integer type, 
							  @FormDataParam("comment") String comment) throws Exception {
		
		ToolsUploadCriteriaResp resp = new ToolsUploadCriteriaResp();
		int status = 200;
		
		try {
			LOG.debug("Call uploadDoc");
			service.uploadDoc(uploadedInputStream, fileDetail, productId, contractNo, type, comment);
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp.setStatusCode(1000);
			status = 1000;
		}		
		
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/findUploadDoc")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp findUploadDoc(DocumentFindCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.findUploadDoc(req);
		} catch (Exception e) {
			resp = new DocumentFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/deleteDoc")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp deleteDoc(@QueryParam("productId") String productId, @QueryParam("id") String id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new DocumentFindCriteriaResp(){};
		
		try {
			service.deleteDoc(productId, id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/downloadDoc")
	public Response downloadDoc(@QueryParam("productId") String productId, @QueryParam("id") String id) throws Exception {
		try {
			LOG.debug("Get file");
			final String filePath = service.getFilePath(productId, id);
			String fileName = FilenameUtils.getName(filePath);
			
			StreamingOutput resp = new StreamingOutput() {
				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException {
					ByteArrayInputStream in = null;
					OutputStream out = null;
					
					try {
						LOG.debug("Get byte");
						java.nio.file.Path path = Paths.get(filePath);
						byte[] data = Files.readAllBytes(path);								
						in = new ByteArrayInputStream(data);
						out = new BufferedOutputStream(output);
						int bytes;
						
						while ((bytes = in.read()) != -1) {
							out.write(bytes);
						}
					} catch (Exception e) {
						LOG.error(e.toString());
						throw e;
					} finally {
						try {if(in != null) in.close();} catch (Exception e2) {}
						try {if(out != null) out.close();} catch (Exception e2) {}
					}
				}
			};
			
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(fileName));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
}