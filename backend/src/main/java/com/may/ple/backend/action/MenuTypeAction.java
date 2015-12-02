package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.MenuTypeLoadCriteriaResp;
import com.may.ple.backend.criteria.MenuTypePersistCriteriaReq;
import com.may.ple.backend.criteria.MenuTypePersistCriteriaResp;
import com.may.ple.backend.entity.MenuType;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.MenuTypeService;

@Component
@Path("menuType")
public class MenuTypeAction {
	private static final Logger LOG = Logger.getLogger(MenuTypeAction.class.getName());
	private MenuTypeService menuTypeService;
	
	@Autowired
	public MenuTypeAction(MenuTypeService menuTypeService) {
		this.menuTypeService = menuTypeService;
	}
	
	@GET
	@Path("/loadMenuType")
	public MenuTypeLoadCriteriaResp loadMenuType() {
		LOG.debug("Start");
		MenuTypeLoadCriteriaResp resp = new MenuTypeLoadCriteriaResp();
		
		try {			
			List<MenuType> menuTypes = menuTypeService.loadMenuType();
			resp.setMenuTypes(menuTypes);
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
	public MenuTypePersistCriteriaResp saveAndUpdate(MenuTypePersistCriteriaReq req) {
		LOG.debug("Start");
		MenuTypePersistCriteriaResp resp = new MenuTypePersistCriteriaResp();
		try {			
			LOG.debug(req);
			
			Long id = menuTypeService.persistMenuType(req);
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
	@Path("/deleteMenuType")
	public CommonCriteriaResp deleteMenuType(@QueryParam("id") Long id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {			
			LOG.debug(id);
			
			menuTypeService.deleteMenuType(id);
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
