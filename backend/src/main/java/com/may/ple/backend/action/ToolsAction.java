package com.may.ple.backend.action;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.catalina.util.URLEncoder;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.constant.ConvertTypeConstant;
import com.may.ple.backend.criteria.ToolsExcel2TextCriteriaResp;
import com.may.ple.backend.criteria.ToolsUploadCriteriaResp;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.service.ToolsService;
import com.may.ple.backend.utils.FileUtil;

@Component
@Path("tools")
public class ToolsAction {
	private static final Logger LOG = Logger.getLogger(ToolsAction.class.getName());
	private ToolsService service;
	
	@Autowired
	public ToolsAction(ToolsService service) {
		this.service = service;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response excel2txt(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("type") Integer type) throws Exception {		
		ToolsUploadCriteriaResp resp = new ToolsUploadCriteriaResp();
		int status = 200;
		
		try {
			LOG.debug("Get Filename");
			Date now = Calendar.getInstance().getTime();
			FileDetail fd = FileUtil.getFileName2(fileDetail, now);
			ConvertTypeConstant fileType = ConvertTypeConstant.findById(type);
			
			if(fileType == ConvertTypeConstant.ELS_TXT) {
				service.excel2txt(uploadedInputStream, fileDetail, fd, fileType);				
			} else if(fileType == ConvertTypeConstant.PDF_JPG) {
				service.pdf2img(uploadedInputStream, fileDetail, fd, fileType);
			} else {
				throw new Exception("Type miss match");
			}
			
			resp.setFileName(fd.fileName + "." + fileType.getExt());
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp.setStatusCode(1000);
			status = 1000;
		}		
		
		return Response.status(status).entity(resp).build();
	}
	
	@GET
	@Path("/download")
	public Response download(@QueryParam("fileName") String fileName) throws Exception {
		try {			
			ToolsExcel2TextCriteriaResp resp = new ToolsExcel2TextCriteriaResp();
			
			byte[] data = service.getFile(fileName);
			resp.setData(data);
			
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(fileName));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
}