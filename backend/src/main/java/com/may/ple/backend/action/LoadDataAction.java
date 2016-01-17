package com.may.ple.backend.action;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.GetMenuCriteriaResp;
import com.may.ple.backend.criteria.GetMenuTypeCriteriaResp;
import com.may.ple.backend.criteria.LoadDataCriteriaResp;
import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.service.LoadDataService;

@Component
@Path("loadData")
public class LoadDataAction {
	private static final Logger LOG = Logger.getLogger(LoadDataAction.class.getName());
	private LoadDataService service;
	
	@Autowired
	public LoadDataAction(LoadDataService service) {
		this.service = service;
	}
	
	@GET
	@Path("/getMenuType")
	public GetMenuTypeCriteriaResp getMenuType() {
		LOG.debug("Start");
		GetMenuTypeCriteriaResp resp = new GetMenuTypeCriteriaResp();
		
		try {
			resp = service.getMenuType();			
		} catch (Exception e) {
			resp = new GetMenuTypeCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getMenus")
	public GetMenuCriteriaResp getMenus(@QueryParam("id") long id) {
		LOG.debug("Start");
		GetMenuCriteriaResp resp = new GetMenuCriteriaResp();
		
		try {
			
			LOG.debug("id: " + id);
			List<Menu> menus = service.getMenus(id);
			resp.setMenus(menus);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/load")
	public LoadDataCriteriaResp load() {
		LOG.debug("Start");
		LoadDataCriteriaResp resp = new LoadDataCriteriaResp();
		
		try {
			
			Map<String, List<Menu>> menus = service.loadMenu();
			resp.setMenus(menus);			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}
