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

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.GetColumnFormatsCriteriaResp;
import com.may.ple.backend.criteria.PersistProductCriteriaReq;
import com.may.ple.backend.criteria.ProductSearchCriteriaReq;
import com.may.ple.backend.criteria.ProductSearchCriteriaResp;
import com.may.ple.backend.entity.ColumFormat;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.ProductService;

@Component
@Path("product")
public class ProductAction {
	private static final Logger LOG = Logger.getLogger(ProductAction.class.getName());
	private ProductService service;
	
	@Autowired
	public ProductAction(ProductService service) {
		this.service = service;
	}
	
	@POST
	@Path("/findProduct")
	@Produces(MediaType.APPLICATION_JSON)
	public ProductSearchCriteriaResp findProduct(ProductSearchCriteriaReq req) {
		LOG.debug("Start");
		ProductSearchCriteriaResp resp = null;
		
		try {
			
			LOG.debug(req);
			resp = service.findProduct(req);
			
		} catch (Exception e) {
			resp = new ProductSearchCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	
	@POST
	@Path("/saveProduct")
	public CommonCriteriaResp saveProduct(PersistProductCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.saveProduct(req);
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
	
	@POST
	@Path("/updateProduct")
	public CommonCriteriaResp updateProduct(PersistProductCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateProduct(req);
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
	
	@POST
	@Path("/deleteProduct")
	public ProductSearchCriteriaResp deleteProduct(ProductSearchCriteriaReq req) {
		LOG.debug("Start");
		ProductSearchCriteriaResp resp;
		
		try {
			LOG.debug(req);
			service.deleteProduct(req.getProdId());
			
			resp = findProduct(req);
		} catch (Exception e) {
			resp = new ProductSearchCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateDatabaseConf")
	public CommonCriteriaResp updateDatabaseConf(PersistProductCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			service.updateDatabaseConf(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateColumnFormat")
	public CommonCriteriaResp updateColumnFormat(PersistProductCriteriaReq req) {
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
	@Path("/getColumnFormat")
	public GetColumnFormatsCriteriaResp getColumnFormat(@QueryParam("id") String id) {
		LOG.debug("Start");
		GetColumnFormatsCriteriaResp resp = new GetColumnFormatsCriteriaResp();
		
		try {
			LOG.debug(id);
			List<ColumFormat> columnFormats = service.getColumnFormat(id);
			resp.setColumnFormats(columnFormats);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}

}
