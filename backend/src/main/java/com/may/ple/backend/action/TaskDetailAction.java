package com.may.ple.backend.action;

import java.io.InputStream;
import java.util.ArrayList;
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
import com.may.ple.backend.bussiness.ExcelReport;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.DymListFindCriteriaReq;
import com.may.ple.backend.criteria.NewTaskCriteriaResp;
import com.may.ple.backend.criteria.NewTaskDownloadCriteriaResp;
import com.may.ple.backend.criteria.TagsCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailCriteriaResp;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaResp;
import com.may.ple.backend.criteria.TaskUpdateByIdsCriteriaReq;
import com.may.ple.backend.criteria.TaskUpdateDetailCriteriaReq;
import com.may.ple.backend.criteria.UpdateTaskIsActiveCriteriaReq;
import com.may.ple.backend.criteria.UpdateTaskIsActiveCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.YearType;
import com.may.ple.backend.service.DymListService;
import com.may.ple.backend.service.NewTaskService;
import com.may.ple.backend.service.TaskDetailService;

@Component
@Path("taskDetail")
public class TaskDetailAction {
	private static final Logger LOG = Logger.getLogger(TaskDetailAction.class.getName());
	private NewTaskService newTaskService;
	private TaskDetailService service;
	private DymListService dymService;
	private ExcelReport excelUtil;
	
	@Autowired
	public TaskDetailAction(TaskDetailService service, NewTaskService newTaskService, 
							DymListService dymService, ExcelReport excelUtil) {
		this.service = service;
		this.newTaskService = newTaskService;
		this.dymService = dymService;
		this.excelUtil = excelUtil;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public TaskDetailCriteriaResp find(TaskDetailCriteriaReq req) {
		LOG.debug("Start");
		TaskDetailCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.find(req, null);
		} catch (Exception e) {
			resp = new TaskDetailCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/exportByCriteria")
	public Response exportByCriteria(TaskDetailCriteriaReq req) throws Exception {
		try {
			LOG.debug(req);
			
			LOG.debug("Get file");
			Map<String, String> map = newTaskService.getFirstTaskFile(req.getProductId(), req.getFileId());
			String fileName = map.get("fileName");
			String filePath = map.get("filePath");
			
			NewTaskDownloadCriteriaResp resp = new NewTaskDownloadCriteriaResp();
			
			resp.setIsCheckData(true);
			resp.setIsByCriteria(true);
			resp.setFilePath(filePath);
			resp.setService(service);
			resp.setDymService(dymService);
			resp.setReq(req);
			resp.setExcelUtil(excelUtil);
			
			ResponseBuilder response = Response.ok(resp);
			response.header("fileName", new URLEncoder().encode(fileName));
			
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	
	@POST
	@Path("/view")
	@Produces(MediaType.APPLICATION_JSON)
	public TaskDetailViewCriteriaResp view(TaskDetailViewCriteriaReq req) {
		LOG.debug("Start");
		TaskDetailViewCriteriaResp resp;
		
		try {
			LOG.debug(req);
			
			resp = service.view(req);
			
			List<Integer> statuses = new ArrayList<>();
			statuses.add(1);
			
			if(req.getIsInit() != null && req.getIsInit()) {
				//--: Dynamic List
				LOG.debug("get Dynamic List");
				DymListFindCriteriaReq reqDym = new DymListFindCriteriaReq();
				reqDym.setStatuses(statuses);
				reqDym.setProductId(req.getProductId());
				List<Map> dymList = dymService.findFullList(reqDym, false);
				resp.setDymList(dymList);
			}
		} catch (Exception e) {
			resp = new TaskDetailViewCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/taskAssigningBySelected")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp taskAssigningBySelected(TaskDetailCriteriaReq req) {
		LOG.debug("Start");
		TaskDetailCriteriaResp resp;
		
		try {
			LOG.debug(req);
			
			LOG.debug("Call taskAssigning");
			service.taskAssigningBySelected(req);
			LOG.debug("Return taskAssigning");
			
			resp = find(req);
		} catch (Exception e) {
			resp = new TaskDetailCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/taskAssigningWhole")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp taskAssigningWhole(TaskDetailCriteriaReq req) {
		LOG.debug("Start");
		TaskDetailCriteriaResp resp;
		
		try {
			LOG.debug(req);
			
			LOG.debug("Call taskAssigning");
			service.taskAssigningWhole(req);
			LOG.debug("Return taskAssigning");
			
			resp = find(req);
		} catch (Exception e) {
			resp = new TaskDetailCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateTaskIsActive")
	@Produces(MediaType.APPLICATION_JSON)
	public UpdateTaskIsActiveCriteriaResp updateTaskIsActive(UpdateTaskIsActiveCriteriaReq req) {
		LOG.debug("Start");
		UpdateTaskIsActiveCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.updateTaskIsActive(req);
		} catch (Exception e) {
			resp = new UpdateTaskIsActiveCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/uploadUpdate")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadUpdate(@FormDataParam("file") InputStream uploadedInputStream, 
									@FormDataParam("file") FormDataContentDisposition fileDetail, 
									@FormDataParam("productId") String productId,
									@FormDataParam("taskFileId") String taskFileId,
									@FormDataParam("isConfirmImport") Boolean isConfirmImport,
								    @FormDataParam("yearTypes") String yearTypes) {
		LOG.debug("Start");
		NewTaskCriteriaResp resp = new NewTaskCriteriaResp();
		
		try {
			List<YearType> yearT = null;
			
			if(isConfirmImport != null && isConfirmImport && yearTypes != null) {
				LOG.info("Parse yearType");
				yearT = Arrays.asList(new Gson().fromJson(yearTypes, YearType[].class));
			}
			
			LOG.debug("Call uploadUpload");
			Map<String, Object> colData = service.uploadUpload(uploadedInputStream, fileDetail, productId, taskFileId, isConfirmImport, yearT);
			
			if(colData != null) {				
				resp.setColDateTypes((List<ColumnFormat>)colData.get("colDateTypes"));
				resp.setColNotFounds((List<String>)colData.get("colNotFounds"));
				resp.setUpdatedNo((Integer)colData.get("updatedNo"));
			}
		} catch (CustomerException e) {
			if(e.errCode == 3000) {
				resp.setCommonMsg(e.getMessage());
			}
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp.setStatusCode(1000);
		}
		
		LOG.debug("End");
		return Response.status(200).entity(resp).build();
	}
	
	@POST
	@Path("/taskUpdateByIds")
	@Produces(MediaType.APPLICATION_JSON)
	public TaskDetailCriteriaResp taskUpdateByIds(TaskUpdateByIdsCriteriaReq req) {
		LOG.debug("Start");
		TaskDetailCriteriaResp resp;
		
		try {
			LOG.debug(req);
			
			if(req.getActionType().equals("enable")) {				
				service.taskEnableDisable(req.getTaskIds(), req.getProductId(), true);
			} else if(req.getActionType().equals("disable")) {
				service.taskEnableDisable(req.getTaskIds(), req.getProductId(), false);
			} else if(req.getActionType().equals("remove")) {
				service.taskRemoveByIds(req.getTaskIds(), req.getProductId());
			}
			
			TaskDetailCriteriaReq detailCriteriaReq = new TaskDetailCriteriaReq();
			detailCriteriaReq.setCurrentPage(req.getCurrentPage());
			detailCriteriaReq.setItemsPerPage(req.getItemsPerPage());
			detailCriteriaReq.setTaskFileId(req.getTaskFileId());
			detailCriteriaReq.setProductId(req.getProductId());
			detailCriteriaReq.setColumnName(req.getColumnName());
			detailCriteriaReq.setOrder(req.getOrder());
			detailCriteriaReq.setKeyword(req.getKeyword());
			detailCriteriaReq.setOwner(req.getOwner());
			detailCriteriaReq.setIsActive(req.getIsActive());
			detailCriteriaReq.setFromPage(req.getFromPage());
			detailCriteriaReq.setDateColumnName(req.getDateColumnName());
			detailCriteriaReq.setDateFrom(req.getDateFrom());
			detailCriteriaReq.setDateTo(req.getDateTo());			
			
			resp = service.find(detailCriteriaReq, null);
		} catch (Exception e) {
			resp = new TaskDetailCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateTaskData")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateTaskData(TaskUpdateDetailCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateTaskData(req);
		} catch (Exception e) {
			resp = new TaskDetailCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateTags")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp updateTags(TagsCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateTags(req);
		} catch (Exception e) {
			resp = new TaskDetailCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/taskDisable")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp taskDisable(TaskUpdateByIdsCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.taskEnableDisable(req.getTaskIds(), req.getProductId(), false);
		} catch (Exception e) {
			resp = new TaskDetailCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}
