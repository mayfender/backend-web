/*package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.TableLandPersistCriteriaReq;
import com.may.ple.backend.criteria.TableLandPersistCriteriaResp;
import com.may.ple.backend.criteria.TableLandSearchCriteriaReq;
import com.may.ple.backend.criteria.TableLandSearchCriteriaResp;
import com.may.ple.backend.entity.TableLand;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.TableLandService;

@Component
@Path("table")
public class TableLandAction {
	private static final Logger LOG = Logger.getLogger(TableLandAction.class.getName());
	private TableLandService tableLandService;
	
	@Autowired
	public TableLandAction(TableLandService tableLandService) {
		this.tableLandService = tableLandService;
	}
	
	@POST
	@Path("/searchTable")
	public TableLandSearchCriteriaResp searchTable(TableLandSearchCriteriaReq req) {
		LOG.debug("Start");
		TableLandSearchCriteriaResp resp = new TableLandSearchCriteriaResp();
		
		try {			
			LOG.debug(req);
			
			List<TableLand> tables = tableLandService.searchTableLand(req);
			resp.setTables(tables);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/saveAndUpdate")
	public TableLandPersistCriteriaResp saveAndUpdate(TableLandPersistCriteriaReq req) {
		LOG.debug("Start");
		TableLandPersistCriteriaResp resp = new TableLandPersistCriteriaResp();
		
		try {			
			LOG.debug(req);
			
			Long id = tableLandService.persistTable(req);
			resp.setId(id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/deleteTable")
	public CommonCriteriaResp deleteTable(@QueryParam("id") Long id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {			
			LOG.debug(id);
			
			tableLandService.deleteMenuType(id);
		} catch (CustomerException cx) {
			resp.setStatusCode(cx.errCode);
			LOG.error(cx.toString());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}

}
*/