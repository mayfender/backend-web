package com.may.ple.backend.action;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.catalina.util.URLEncoder;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.ExportTemplateFindCriteriaResp;
import com.may.ple.backend.criteria.NewTaskCriteriaReq;
import com.may.ple.backend.criteria.NewTaskCriteriaResp;
import com.may.ple.backend.criteria.NewTaskDownloadCriteriaResp;
import com.may.ple.backend.criteria.NewTaskUpdateCriteriaReq;
import com.may.ple.backend.criteria.TraceResultReportFindCriteriaReq;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.YearType;
import com.may.ple.backend.service.NewTaskService;

@Component
@Path("newTask")
public class NewTaskAction {
	private static final Logger LOG = Logger.getLogger(NewTaskAction.class.getName());
	private NewTaskService service;
	private DbFactory dbFactory;
	
	@Autowired
	public NewTaskAction(NewTaskService service, DbFactory dbFactory) {
		this.service = service;
		this.dbFactory = dbFactory;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("file") InputStream uploadedInputStream, 
						   @FormDataParam("file") FormDataContentDisposition fileDetail, 
						   @FormDataParam("currentProduct") String currentProduct, 
						   @FormDataParam("isConfirmImport") Boolean isConfirmImport,
						   @FormDataParam("yearTypes") String yearTypes) {
		LOG.debug("Start");
		NewTaskCriteriaResp resp = null;
		int status = 200;
		
		try {
			LOG.debug(currentProduct);
			List<YearType> yearT = null;
			
			if(isConfirmImport != null && isConfirmImport && yearTypes != null) {
				LOG.info("Parse yearType");
				yearT = Arrays.asList(new Gson().fromJson(yearTypes, YearType[].class));
			}
			
			//--: Save to database
			LOG.debug("call save");
			Map<String, Object> colData = service.save(uploadedInputStream, fileDetail, currentProduct, isConfirmImport, yearT);
			
			LOG.debug("Find task to show");
			NewTaskCriteriaReq req = new NewTaskCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			req.setProductId(currentProduct);
			
			resp = service.findAll(req);
			
			if(colData != null) {				
				resp.setColDateTypes((List<ColumnFormat>)colData.get("colDateTypes"));
				resp.setColNotFounds((List<String>)colData.get("colNotFounds"));
			}
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new NewTaskCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
	
	@POST
	@Path("/download")
	public Response download(NewTaskCriteriaReq req) throws Exception {
		try {
			LOG.debug(req);
			
			LOG.debug("Get file");
			Map<String, String> map = service.getTaskFile(req);
			String fileName = map.get("fileName");
			String filePath = map.get("filePath");
			NewTaskDownloadCriteriaResp resp = new NewTaskDownloadCriteriaResp();
			
			if(req.getIsCheckData() != null && req.getIsCheckData()) {
				LOG.debug("Check Data");
				/*MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
				
				Criteria criteria = Criteria.where(SYS_FILE_ID.getName()).is(req.getId());
				Query query = Query.query(criteria);
				query.with(new Sort(SYS_OLD_ORDER.getName()));
				
				List<Map> taskDetails = template.find(query, Map.class, NEW_TASK_DETAIL.getName());		
				resp.setTaskDetails(taskDetails);*/
			}
			
			resp.setIsCheckData(req.getIsCheckData() == null ? false : req.getIsCheckData());
			resp.setFilePath(filePath);
			
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(fileName));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	@POST
	@Path("/findAll")
	@Produces(MediaType.APPLICATION_JSON)
	public NewTaskCriteriaResp findAll(NewTaskCriteriaReq req) {
		LOG.debug("Start");
		NewTaskCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.findAll(req);
		} catch (Exception e) {
			resp = new NewTaskCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/deleteFileTask")
	public NewTaskCriteriaResp deleteFileTask(NewTaskCriteriaReq req) {
		LOG.debug("Start");
		NewTaskCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.deleteFileTask(req.getProductId(), req.getId());
			
			resp = findAll(req);
		} catch (Exception e) {
			resp = new NewTaskCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateEnabled")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateEnabled(NewTaskUpdateCriteriaReq req) {
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
	@Path("/findExportTemplate")
	@Produces(MediaType.APPLICATION_JSON)
	public ExportTemplateFindCriteriaResp findExportTemplate(TraceResultReportFindCriteriaReq req) {
		LOG.debug("Start");
		ExportTemplateFindCriteriaResp resp;
		
		try {
			LOG.debug(req);	
			resp = service.findExportTemplate(req);
		} catch (Exception e) {
			resp = new ExportTemplateFindCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/uploadExportTemplate")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadExportTemplate(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail, 
			@FormDataParam("currentProduct") String currentProduct) {
		LOG.debug("Start");
		ExportTemplateFindCriteriaResp resp = null;
		int status = 200;
		
		try {
			LOG.debug(currentProduct);
			
			//--: Save to database
			LOG.debug("call save");
			service.saveExportTemplate(uploadedInputStream, fileDetail, currentProduct);
			
			LOG.debug("Find task to show");
			TraceResultReportFindCriteriaReq req = new TraceResultReportFindCriteriaReq();
			req.setCurrentPage(1);
			req.setItemsPerPage(10);
			req.setProductId(currentProduct);
			resp = service.findExportTemplate(req);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp = new ExportTemplateFindCriteriaResp(1000);
			status = 1000;
		}
		
		LOG.debug("End");
		return Response.status(status).entity(resp).build();
	}
		
}
