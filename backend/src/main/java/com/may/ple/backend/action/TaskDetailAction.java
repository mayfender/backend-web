package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.TaskDetailCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailCriteriaResp;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaReq;
import com.may.ple.backend.criteria.TaskDetailViewCriteriaResp;
import com.may.ple.backend.criteria.UpdateTaskIsActiveCriteriaReq;
import com.may.ple.backend.criteria.UpdateTaskIsActiveCriteriaResp;
import com.may.ple.backend.service.TaskDetailService;

@Component
@Path("taskDetail")
public class TaskDetailAction {
	private static final Logger LOG = Logger.getLogger(TaskDetailAction.class.getName());
	private TaskDetailService service;
	
	@Autowired
	public TaskDetailAction(TaskDetailService service) {
		this.service = service;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public TaskDetailCriteriaResp find(TaskDetailCriteriaReq req) {
		LOG.debug("Start");
		TaskDetailCriteriaResp resp;
		
		try {
			LOG.debug(req);
			resp = service.find(req);
		} catch (Exception e) {
			resp = new TaskDetailCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
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
	
}
