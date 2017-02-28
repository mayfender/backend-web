package com.may.ple.backend.action;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.DymListFindCriteriaReq;
import com.may.ple.backend.criteria.DymListFindCriteriaResp;
import com.may.ple.backend.criteria.ListSaveCriteriaReq;
import com.may.ple.backend.criteria.ListSaveCriteriaResp;
import com.may.ple.backend.entity.DymList;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.DymListService;

@Component
@Path("dymList")
public class DymListAction {
	private static final Logger LOG = Logger.getLogger(DymListAction.class.getName());
	private DymListService service;
	
	@Autowired
	public DymListAction(DymListService service) {
		this.service = service;
	}
	
	@POST
	@Path("/findList")
	@Produces(MediaType.APPLICATION_JSON)
	public DymListFindCriteriaResp findActionCode(DymListFindCriteriaReq req) {
		LOG.debug("Start");
		DymListFindCriteriaResp resp = new DymListFindCriteriaResp();
		
		try {
			
			LOG.debug(req);
			
			List<Integer> statuses = new ArrayList<>();
			statuses.add(0);
			statuses.add(1);
			
			req.setStatuses(statuses);
			
			List<DymList> dymList = service.findList(req);
			resp.setDymList(dymList);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/saveList")
	public ListSaveCriteriaResp saveList(ListSaveCriteriaReq req) {
		LOG.debug("Start");
		ListSaveCriteriaResp resp = new ListSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String id = service.saveList(req);
			
			resp.setId(id);
		} catch (CustomerException cx) {
			resp.setStatusCode(cx.errCode);
			LOG.error(cx.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	
	@GET
	@Path("/deleteList")
	public CommonCriteriaResp deleteList(@QueryParam("id")String id, @QueryParam("productId")String productId) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			service.deleteList(id, productId);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
}