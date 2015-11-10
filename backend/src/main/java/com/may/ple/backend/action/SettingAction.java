package com.may.ple.backend.action;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.SettingCriteriaResp;
import com.may.ple.backend.criteria.SettingUpdateCriteriaReq;
import com.may.ple.backend.custom.PropertiesCustom;
import com.may.ple.backend.service.ConfService;

@Component
@Path("setting")
public class SettingAction {
	private static final Logger LOG = Logger.getLogger(SettingAction.class.getName());
	private PropertiesCustom dbProp;
	private ConfService confService;
	
	@Autowired
	public SettingAction(PropertiesCustom dbProp, ConfService confService) {
		this.dbProp = dbProp;
		this.confService = confService;
	}
	
	@GET
	@Path("/loadSetting")
	@Secured("ROLE_ADMIN")
	public SettingCriteriaResp loadSetting() {
		SettingCriteriaResp resp = new SettingCriteriaResp();
		LOG.debug("Start");
		
		try {
			resp.setAfterPriceRate(dbProp.getInt("after.price.rate"));
			resp.setBeforeHour(dbProp.getInt("before.hour"));
			resp.setBeforeHourPriceRate(dbProp.getInt("before.hour.price.rate"));
			resp.setMinuteToHour(dbProp.getInt("minute.to.hour"));
			resp.setPricePerTime(dbProp.getInt("price.per.time"));
			resp.setUnlimtedTime(dbProp.getBoolean("unlimted.time"));
			resp.setParkingSize(dbProp.getInt("parking.size"));
			
			LOG.debug(resp);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString());
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/updateSetting")
	@Secured("ROLE_ADMIN")
	public CommonCriteriaResp updateSetting(SettingUpdateCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			LOG.debug(req);
			
			confService.updateSetting(req);
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString());
		}
		
		LOG.debug("End");
		return resp;
	}
	
}
