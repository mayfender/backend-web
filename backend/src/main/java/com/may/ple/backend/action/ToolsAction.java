package com.may.ple.backend.action;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.catalina.util.URLEncoder;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.ToolsExcel2TextCriteriaResp;
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
	@Path("/excel2txt")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response excel2txt(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) throws Exception {		
		try {
			LOG.debug("Get Filename");
			FileDetail fd = FileUtil.getFileName(fileDetail, null);
			
			ByteArrayOutputStream data = service.excel2txt(uploadedInputStream, fileDetail, fd);
			
			ToolsExcel2TextCriteriaResp resp = new ToolsExcel2TextCriteriaResp();
			resp.setData(data.toByteArray());
			
			int index = fd.fileName.lastIndexOf(".");
			String name = fd.fileName.substring(0, index);
			
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(name + ".txt"));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}		
	}
	
}