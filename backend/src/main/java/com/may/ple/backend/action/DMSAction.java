package com.may.ple.backend.action;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.DMSCriteriaReq;
import com.may.ple.backend.criteria.DMSCriteriaResp;
import com.may.ple.backend.service.DMSService;

@Component
@Path("dms")
public class DMSAction {
	private static final Logger LOG = Logger.getLogger(DMSAction.class.getName());
	private DMSService service;

	@Autowired
	public DMSAction(DMSService service) {
		this.service = service;
	}

	@POST
	@Path("/getCustomers")
	@Produces(MediaType.APPLICATION_JSON)
	public DMSCriteriaResp getCustomers(DMSCriteriaReq req) {
		LOG.debug("Start");
		DMSCriteriaResp resp;

		try {
			if(StringUtils.isNotBlank(req.getDeletedId())) {
				service.removeCustomer(req.getDeletedId());
			}

			resp = service.getCustomers(req);
		} catch (Exception e) {
			resp = new DMSCriteriaResp();
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@GET
	@Path("/editCustomer")
	@Produces(MediaType.APPLICATION_JSON)
	public DMSCriteriaResp editCustomer(@QueryParam("id")String id) {
		LOG.debug("Start");
		DMSCriteriaResp resp = new DMSCriteriaResp();

		try {
			resp.setCustomer(service.editCustomerById(id));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/updateCustomer")
	@Produces(MediaType.APPLICATION_JSON)
	public DMSCriteriaResp updateCustomer(DMSCriteriaReq req) {
		LOG.debug("Start");
		DMSCriteriaResp resp = new DMSCriteriaResp();

		try {
			String id = service.updateCustomer(req);
			resp.setId(id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/updateProduct")
	@Produces(MediaType.APPLICATION_JSON)
	public DMSCriteriaResp updateProduct(DMSCriteriaReq req) {
		LOG.debug("Start");
		DMSCriteriaResp resp = new DMSCriteriaResp();

		try {
			service.updateProduct(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@GET
	@Path("/removeProduct")
	@Produces(MediaType.APPLICATION_JSON)
	public DMSCriteriaResp removeProduct(@QueryParam("productId")String productId, @QueryParam("id")String id) {
		LOG.debug("Start");
		DMSCriteriaResp resp = new DMSCriteriaResp();

		try {
			service.removeProduct(productId, id);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	/*@GET
	@Path("/getCustomers")
	@Produces(MediaType.APPLICATION_JSON)
	public DMSCriteriaResp getCustomers(@QueryParam("enabled")Boolean enabled, @QueryParam("name")String name) {
		LOG.debug("Start");
		DMSCriteriaResp resp = new DMSCriteriaResp();

		try {

			List<Map> customers = service.getCustomers(enabled, name);
			resp.setCustomers(customers);

		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}*/



	/*@POST
	@Path("/persistDealer")
	@Produces(MediaType.APPLICATION_JSON)
	public DealerCriteriaResp persistDealer(DealerCriteriaReq req) {
		LOG.debug("Start");
		DealerCriteriaResp resp = new DealerCriteriaResp();

		try {
			service.persistDealer(req);
			resp.setDealers(service.getDealer(new DealerCriteriaReq()));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}*/

}
