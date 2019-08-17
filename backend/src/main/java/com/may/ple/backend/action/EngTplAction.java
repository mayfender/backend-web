package com.may.ple.backend.action;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
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
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.EngTplCriteriaReq;
import com.may.ple.backend.criteria.EngTplCriteriaResp;
import com.may.ple.backend.service.EngTplService;

@Component
@Path("engTpl")
public class EngTplAction {
	private static final Logger LOG = Logger.getLogger(EngTplAction.class.getName());
	private EngTplService service;
	
	@Autowired
	public EngTplAction(EngTplService service) {
		this.service = service;
	}
	
	@GET
	@Path("/getTpl")
	public EngTplCriteriaResp getTpl(
			@QueryParam("currentPage")int currentPage,
			@QueryParam("itemsPerPage")int itemsPerPage,
			@QueryParam("type")int type,
			@QueryParam("prodId")String prodId) {
		
		EngTplCriteriaResp resp = null;
		try {
			resp = service.getTpl(currentPage, itemsPerPage, type, prodId, null);
		} catch (Exception e) {
			resp = new EngTplCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		return resp;
	}
	
	@POST
	@Path("/uploadTpl")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadTpl(
			@FormDataParam("file") InputStream uploadedInputStream, 
			@FormDataParam("file") FormDataContentDisposition fileDetail, 
			@FormDataParam("prodId") String prodId,
			@FormDataParam("type") int type) {
		
		EngTplCriteriaResp resp = null;
		int status = 200;
		try {
			//--: Save to database
			LOG.debug("call save");
			service.save(uploadedInputStream, fileDetail, prodId, type);
			
			//--: Get TPL to show
			resp = service.getTpl(1, 10, type, prodId, true);
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new EngTplCriteriaResp(1000);
			status = 1000;
		}
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/updateTemplateName")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateTemplateName(EngTplCriteriaReq req) {
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
	@Path("/delete")
	public CommonCriteriaResp delete(EngTplCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.delete(req.getProductId(), req.getId());
			resp = getTpl(req.getCurrentPage(), req.getItemsPerPage(), req.getType(), req.getProductId());
		} catch (Exception e) {
			resp = new CommonCriteriaResp(1000){};
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateEnabled")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateEnabled(EngTplCriteriaReq req) {
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
		return resp;
	}
	
	@POST
	@Path("/download")
	public Response download(EngTplCriteriaReq req) throws Exception {
		try {
			final String filePath = service.getFile(req);
			StreamingOutput out = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					OutputStream out = null;
					ByteArrayInputStream in = null;
					try {
						out = new BufferedOutputStream(os);
						java.nio.file.Path path = Paths.get(filePath);
						byte[] data = Files.readAllBytes(path);								
						in = new ByteArrayInputStream(data);
						int bytes;
						
						while ((bytes = in.read()) != -1) {
							out.write(bytes);
						}
					} catch (Exception e) {
						LOG.error(e.toString(), e);
					} finally {
						try {if(in != null) in.close();} catch (Exception e2) {}
						try {if(out != null) out.close();} catch (Exception e2) {}
					}
				}
			};
	
			ResponseBuilder response = Response.ok(out);
			response.header("fileName", new URLEncoder().encode(new File(filePath).getName()));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
}