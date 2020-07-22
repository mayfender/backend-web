package com.may.ple.backend.action;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.DealerCriteriaReq;
import com.may.ple.backend.criteria.DealerCriteriaResp;
import com.may.ple.backend.service.DealerService;

@Component
@Path("dealer")
public class DealerAction {
	private static final Logger LOG = Logger.getLogger(DealerAction.class.getName());
	private DealerService service;

	@Autowired
	public DealerAction(DealerService service) {
		this.service = service;
	}

	@GET
	@Path("/getDealerAll")
	@Produces(MediaType.APPLICATION_JSON)
	public DealerCriteriaResp getDealerAll() {
		LOG.debug("Start");
		DealerCriteriaResp resp = new DealerCriteriaResp();

		try {
			resp.setDealers(service.getDealer(new DealerCriteriaReq()));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
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
	}

	@POST
	@Path("/remove")
	@Produces(MediaType.APPLICATION_JSON)
	public DealerCriteriaResp remove(DealerCriteriaReq req) {
		LOG.debug("Start");
		DealerCriteriaResp resp = new DealerCriteriaResp();

		try {
			service.remove(req);
			resp.setDealers(service.getDealer(new DealerCriteriaReq()));
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}











	/*@POST
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
	@Path("/updatePayType")
	public CommonCriteriaResp updatePayType(PersistProductCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};

		try {
			LOG.debug(req);
			service.updatePayType(req);
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
	@Path("/getProductSetting")
	public CommonCriteriaResp getProductSetting(@QueryParam("productId")String productId) {
		LOG.debug("Start");
		ProductSettingCriteriaResp resp = new ProductSettingCriteriaResp();

		try {
			ProductSetting productSetting = service.getProductSetting(productId);
			resp.setProductSetting(productSetting);
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

	@POST
	@Path("/updateNotice")
	public CommonCriteriaResp updateNotice(ProductNoticeUpdateCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};

		try {
			LOG.debug(req);
			service.updateNotice(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/updateColumnName")
	public CommonCriteriaResp updateColumnName(UpdateProductSettingCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};

		try {
			LOG.debug(req);
			service.updateColumnName(req);
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
			Product product = service.getProduct(id);
			resp.setColumnFormats(product.getColumnFormats());
			ProductSetting setting = product.getProductSetting();

			resp.setContractNoColumnName(setting.getContractNoColumnName());
			resp.setIdCardNoColumnName(setting.getIdCardNoColumnName());
			resp.setBalanceColumnName(setting.getBalanceColumnName());
			resp.setExpirationDateColumnName(setting.getExpirationDateColumnName());
			resp.setBirthDateColumnName(setting.getBirthDateColumnName());
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@GET
	@Path("/getColumnFormatPayment")
	public GetColumnFormatsCriteriaResp getColumnFormatPayment(@QueryParam("id") String id) {
		LOG.debug("Start");
		GetColumnFormatsCriteriaResp resp = new GetColumnFormatsCriteriaResp();

		try {
			LOG.debug(id);
			Product product = service.getProduct(id);
			resp.setColumnFormats(product.getColumnFormatsPayment());
			ProductSetting setting;

			if((setting = product.getProductSetting()) != null) {
				resp.setContractNoColumnName(setting.getContractNoColumnNamePayment());
				resp.setIdCardNoColumnName(setting.getIdCardNoColumnNamePayment());
				resp.setSortingColumnName(setting.getSortingColumnNamePayment());
				resp.setPaidDateColumnName(setting.getPaidDateColumnNamePayment());
			}
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@GET
	@Path("/getColumnFormatDet")
	public GetColumnFormatsDetCriteriaResp getColumnFormatDet(@QueryParam("productId") String id) {
		LOG.debug("Start");
		GetColumnFormatsDetCriteriaResp resp;

		try {
			LOG.debug(id);
			resp = service.getColumnFormatDet(id);
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
	@Path("/updateWorkingTime")
	public CommonCriteriaResp updateWorkingTime(WorkingTimeUpdateCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};

		try {
			LOG.debug(req);
			service.updateWorkingTime(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@GET
	@Path("/getWorkingTime")
	public WorkingTimeCriteriaResp getWorkingTime(@QueryParam("productId")String productId) {
		LOG.debug("Start");
		WorkingTimeCriteriaResp resp;

		try {

			resp = service.getWorkingTime(productId);

		} catch (Exception e) {
			resp = new WorkingTimeCriteriaResp(1000);
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/updateProductSetting")
	public CommonCriteriaResp updateProductSetting(PersistProductCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};

		try {
			service.updateProductSetting(req);
		} catch (Exception e) {
			resp = new CommonCriteriaResp(1000){};
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}

	@POST
	@Path("/updateField")
	public CommonCriteriaResp updateField(PersistProductCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};

		try {
			service.updateField(req);
		} catch (Exception e) {
			resp = new CommonCriteriaResp(1000){};
			LOG.error(e.toString(), e);
		}

		LOG.debug("End");
		return resp;
	}*/

}
