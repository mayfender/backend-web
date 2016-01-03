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
import com.may.ple.backend.criteria.FindSubMenuCriteriaResp;
import com.may.ple.backend.criteria.SubMenuPersistCriteriaReq;
import com.may.ple.backend.criteria.SubMenuPersistCriteriaResp;
import com.may.ple.backend.entity.SubMenu;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.SubMenuService;

@Component
@Path("subMenu")
public class SubMenuAction {
	private static final Logger LOG = Logger.getLogger(SubMenuAction.class.getName());
	private SubMenuService subMenuService;
	
	@Autowired
	public SubMenuAction(SubMenuService subMenuService) {
		this.subMenuService = subMenuService;
	}
		
	@GET
	@Path("/findByMenuId")
	public FindSubMenuCriteriaResp findByMenuId(@QueryParam("menuId") Long menuId) {
		LOG.debug("Start");
		FindSubMenuCriteriaResp resp = new FindSubMenuCriteriaResp();
		
		try {			
			LOG.debug(menuId);
			
			List<SubMenu> subMenus = subMenuService.findByMenuId(menuId);
			
			resp.setSubMenus(subMenus);
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
	public SubMenuPersistCriteriaResp saveAndUpdate(SubMenuPersistCriteriaReq req) {
		LOG.debug("Start");
		SubMenuPersistCriteriaResp resp = new SubMenuPersistCriteriaResp();
		try {			
			LOG.debug(req);
			
			Long id = subMenuService.persistSubMenu(req);
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
	@Path("/deleteSubMenu")
	public CommonCriteriaResp deleteSubMenu(@QueryParam("id") Long id) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {			
			LOG.debug(id);
			
			subMenuService.deleteSubMenu(id);
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
