package com.may.ple.backend.action;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.ThailandRegionFindCriteriaResp;
import com.may.ple.backend.entity.Amphures;
import com.may.ple.backend.entity.Districts;
import com.may.ple.backend.entity.Provinces;
import com.may.ple.backend.entity.Zipcodes;
import com.may.ple.backend.service.ThailandRegionService;

@Component
@Path("thaiRegion")
public class ThaiLandRegionAction {
	private static final Logger LOG = Logger.getLogger(ThaiLandRegionAction.class.getName());
	private ThailandRegionService service;
	
	@Autowired
	public ThaiLandRegionAction(ThailandRegionService service) {
		this.service = service;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadAssing(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		LOG.debug("Start");
		CommonCriteriaResp resp = new CommonCriteriaResp() {};
		
		try {
			
			LOG.debug("Start");
			service.upload(uploadedInputStream, fileDetail);
			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			resp.setStatusCode(1000);
		}
		
		LOG.debug("End");
		return Response.status(200).entity(resp).build();
	}
	
	@GET
	@Path("/findProvince")
	public ThailandRegionFindCriteriaResp findProvince(@QueryParam("provinceName")String provinceName) {
		LOG.debug("Start");
		ThailandRegionFindCriteriaResp resp = new ThailandRegionFindCriteriaResp() {};
		
		try {
			
			LOG.debug(provinceName);
			List<Provinces> provinces = service.findProvince(provinceName);
			resp.setProvinces(provinces);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/findAmphure")
	public ThailandRegionFindCriteriaResp findAmphure(@QueryParam("provinceId")Long provinceId) {
		LOG.debug("Start");
		ThailandRegionFindCriteriaResp resp = new ThailandRegionFindCriteriaResp() {};
		
		try {
			
			LOG.debug(provinceId);
			List<Amphures> amphures = service.findAmphure(provinceId);
			resp.setAmphures(amphures);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/findDistrict")
	public ThailandRegionFindCriteriaResp findDistrict(@QueryParam("provinceId")Long provinceId, @QueryParam("amphureId")Long amphureId) {
		LOG.debug("Start");
		ThailandRegionFindCriteriaResp resp = new ThailandRegionFindCriteriaResp() {};
		
		try {
			
			LOG.debug("provinceId: " + provinceId + ", amphureId: " + amphureId);
			List<Districts> districts = service.findDistrict(provinceId, amphureId);
			resp.setDistricts(districts);
			
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	@GET
	@Path("/findZipcode")
	public ThailandRegionFindCriteriaResp findZipcode(@QueryParam("districtCode")String districtCode) {
		LOG.debug("Start");
		ThailandRegionFindCriteriaResp resp = new ThailandRegionFindCriteriaResp() {};
		
		try {
			
			LOG.debug("districtCode: " + districtCode);
			Zipcodes zipcode = service.findZipcode(districtCode);
			
			if(zipcode != null) {
				resp.setZipcode(zipcode.getZipcode());				
			}
		} catch (Exception e) {
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug("End");
		return resp;
	}
	
	
}