package com.may.ple.backend.action;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OLD_ORDER;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.may.ple.backend.constant.ConvertTypeConstant;
import com.may.ple.backend.criteria.NewTaskDownloadCriteriaResp;
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
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response excel2txt(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("type") Integer type) throws Exception {		
		try {
			LOG.debug("Get Filename");
			FileDetail fd = FileUtil.getFileName(fileDetail, null);
			ByteArrayOutputStream data;
			
			if(ConvertTypeConstant.findById(type) == ConvertTypeConstant.ELS_TXT) {
				data = service.excel2txt(uploadedInputStream, fileDetail, fd);				
			} else if(ConvertTypeConstant.findById(type) == ConvertTypeConstant.PDF_JPG) {
				data = service.pdf2img(uploadedInputStream, fileDetail, fd);
			} else {
				throw new Exception("Type miss match");
			}
			
			ToolsExcel2TextCriteriaResp resp = new ToolsExcel2TextCriteriaResp();
			resp.setData(data.toByteArray());
			
			int index = fd.fileName.lastIndexOf(".");
			String name = fd.fileName.substring(0, index);
			
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(name + fd.fileExt));
//			response.header("Content-Type", "text/plain;charset=UTF-8").build();
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}		
	}
	
	@GET
	@Path("/download")
	public Response download() throws Exception {
		try {			
			ByteArrayOutputStream data;
			
			ToolsExcel2TextCriteriaResp resp = new ToolsExcel2TextCriteriaResp();
			resp.setData(data.toByteArray());
			
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(name + fd.fileExt));
//			response.header("Content-Type", "text/plain;charset=UTF-8").build();
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
}