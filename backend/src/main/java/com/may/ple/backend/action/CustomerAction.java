package com.may.ple.backend.action;

import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.CustomerSearchCriteriaReq;
import com.may.ple.backend.criteria.CustomerSearchCriteriaResp;
import com.may.ple.backend.criteria.OpenCashDrawerCriteriaReq;
import com.may.ple.backend.entity.Customer;
import com.may.ple.backend.service.CustomerService;

@Component
@Path("customer")
public class CustomerAction {
	private static final Logger LOG = Logger.getLogger(CustomerAction.class.getName());
	private CustomerService customerService;
	
	@Autowired
	public CustomerAction(CustomerService customerService) {
		this.customerService = customerService;
	}
	
	@POST
	@Path("/searchCus")
	public CustomerSearchCriteriaResp searchCus(CustomerSearchCriteriaReq req) {
		LOG.debug("Start");
		CustomerSearchCriteriaResp resp = new CustomerSearchCriteriaResp();
		
		try {
			LOG.debug(req);
			
			List<Customer> tables = customerService.searchCus(req);
			resp.setCustomers(tables);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/checkBill")
	public CommonCriteriaResp checkBill(OpenCashDrawerCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp(){};
		
		try {
			LOG.debug(req);
			customerService.checkBill(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
}
