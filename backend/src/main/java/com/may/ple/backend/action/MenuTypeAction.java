package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.MenuTypeLoadCriteriaResp;
import com.may.ple.backend.entity.MenuType;
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
			resp = new MenuTypeLoadCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}
