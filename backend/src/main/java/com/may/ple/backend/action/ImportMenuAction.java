package com.may.ple.backend.action;

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

import com.may.ple.backend.criteria.ColumnFormatDetActiveUpdateCriteriaReq;
import com.may.ple.backend.criteria.ColumnFormatDetUpdatreCriteriaReq;
import com.may.ple.backend.criteria.ColumnLinkUpdateCriteriaReq;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.GetColumnFormatsCriteriaResp;
import com.may.ple.backend.criteria.GetColumnFormatsDetCriteriaResp;
import com.may.ple.backend.criteria.GroupDataUpdateCriteriaReq;
import com.may.ple.backend.criteria.ImportMenuDeleteCriteriaReq;
import com.may.ple.backend.criteria.ImportMenuFindCriteriaReq;
import com.may.ple.backend.criteria.ImportMenuFindCriteriaResp;
import com.may.ple.backend.criteria.ImportMenuSaveCriteriaReq;
import com.may.ple.backend.criteria.ImportMenuSaveCriteriaResp;
import com.may.ple.backend.criteria.ImportOthersUpdateColFormCriteriaReq;
import com.may.ple.backend.entity.ImportMenu;
import com.may.ple.backend.service.ImportMenuService;

@Component
@Path("importMenu")
public class ImportMenuAction {
	private static final Logger LOG = Logger.getLogger(ImportMenuAction.class.getName());
	private ImportMenuService service;
	
	@Autowired
	public ImportMenuAction(ImportMenuService service) {
		this.service = service;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public ImportMenuFindCriteriaResp find(ImportMenuFindCriteriaReq req) {
		LOG.debug("Start");
		ImportMenuFindCriteriaResp resp = new ImportMenuFindCriteriaResp();
		
		try {
			LOG.debug(req);
			List<ImportMenu> menus = service.find(req);
			resp.setMenus(menus);;
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/save")
	@Produces(MediaType.APPLICATION_JSON)
	public ImportMenuSaveCriteriaResp save(ImportMenuSaveCriteriaReq req) {
		LOG.debug("Start");
		ImportMenuSaveCriteriaResp resp = new ImportMenuSaveCriteriaResp();
		
		try {
			LOG.debug(req);
			String menuId = service.save(req);
			resp.setMenuId(menuId);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp delete(ImportMenuDeleteCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug(req);
			service.delete(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getColumnFormat")
	public GetColumnFormatsCriteriaResp getColumnFormat(@QueryParam("menuId") String menuId, @QueryParam("productId") String productId) {
		LOG.debug("Start");
		GetColumnFormatsCriteriaResp resp;
		
		try {
			LOG.debug("menuId: " + menuId + ", productId: " + productId);
			resp = service.getColumnFormat(menuId, productId);
			
		} catch (Exception e) {
			resp = new GetColumnFormatsCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateColumnFormat")
	public CommonCriteriaResp updateColumnFormat(ImportOthersUpdateColFormCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateColumnFormat(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/getColumnFormatDet")
	public GetColumnFormatsDetCriteriaResp getColumnFormatDet(@QueryParam("productId")String productId, @QueryParam("menuId") String menuId) {
		LOG.debug("Start");
		GetColumnFormatsDetCriteriaResp resp;
		
		try {
			resp = service.getColumnFormatDet(productId, menuId);
		} catch (Exception e) {
			resp = new GetColumnFormatsDetCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateGroupDatas")
	public CommonCriteriaResp updateGroupDatas(GroupDataUpdateCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateGroupDatas(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateColumnFormatDet")
	public CommonCriteriaResp updateColumnFormatDet(ColumnFormatDetUpdatreCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateColumnFormatDet(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateColumnFormatDetActive")
	public CommonCriteriaResp updateColumnFormatDetActive(ColumnFormatDetActiveUpdateCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateColumnFormatDetActive(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateColumnLink")
	public CommonCriteriaResp updateColumnLink(ColumnLinkUpdateCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateColumnLink(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}

}
