package com.may.ple.backend.action;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.NoticeFindCriteriaResp;
import com.may.ple.backend.criteria.NoticeUpdateCriteriaReq;
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
			req.setProductId(currentProduct);
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
	@Path("/download")
	public Response download(NoticeFindCriteriaReq req) throws Exception {
		try {
			LOG.debug(req);
			
			ResponseBuilder response = Response.ok(getStream(req));
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public NoticeFindCriteriaResp find(NoticeFindCriteriaReq req) {
		LOG.debug("Start");
		NoticeFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			
			if(req.getProductId() == null && req != null && req.getIsInit()) {
				LOG.debug("Find product");
				ProductSearchCriteriaReq prodReq = new ProductSearchCriteriaReq();
				prodReq.setCurrentPage(1);
				prodReq.setItemsPerPage(1000);
				prodReq.setEnabled(1);
				ProductSearchCriteriaResp findProduct = prodService.findProduct(prodReq);
				
				req.setProductId(findProduct.getProducts().get(0).getId());
				
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
	
	@POST
	@Path("/updateTemplateName")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateTemplateName(NoticeUpdateCriteriaReq req) {
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
	@Path("/updateEnabled")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateEnabled(NoticeUpdateCriteriaReq req) {
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
		LOG.debug("End");
		return resp;
	}
	
	
	@POST
	@Path("/deleteNoticeFile")
	public NoticeFindCriteriaResp deleteNoticeFile(NoticeFindCriteriaReq req) {
		LOG.debug("Start");
		NoticeFindCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.deleteFileTask(req.getProductId(), req.getId());
			
			resp = find(req);
		} catch (Exception e) {
			resp = new NoticeFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	private StreamingOutput getStream(final NoticeFindCriteriaReq criteria) {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				OutputStream out = null;
				ByteArrayInputStream in = null;
				
				try {
					LOG.debug("Start");
					
					service.getNoticeFile(criteria);
					
					java.nio.file.Path path = Paths.get("path/to/file");
					byte[] data = Files.readAllBytes(path);
					
					LOG.debug("Got byte");
					
					in = new ByteArrayInputStream(data);
					out = new BufferedOutputStream(os);
					int bytes;
					
					while ((bytes = in.read()) != -1) {
						out.write(bytes);
					}
					
					LOG.debug("End");
				} catch (Exception e) {
					LOG.error(e.toString());
				} finally {
					if(in != null) in.close();			
					if(out != null) out.close();			
				}	
			}
		};
	}
		
}
