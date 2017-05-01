package com.may.ple.backend.action;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.may.ple.backend.constant.NoticeFrameworkConstant;
import com.may.ple.backend.criteria.CommonCriteriaResp;
import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;

@Component
@Path("noticeManager")
public class NoticeManagerAction {
	private static final Logger LOG = Logger.getLogger(NoticeManagerAction.class.getName());
	private MongoTemplate templateCenter;
	private NoticeXDocUploadAction xdocAct;
	private NoticeUploadAction jasperAct;
	
	@Autowired
	public NoticeManagerAction(MongoTemplate templateCenter, NoticeXDocUploadAction xdocAct, NoticeUploadAction jasperAct) {
		this.templateCenter = templateCenter;
		this.xdocAct = xdocAct;
		this.jasperAct = jasperAct;
	}
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public CommonCriteriaResp find(NoticeFindCriteriaReq req) {
		LOG.debug("Start");
		CommonCriteriaResp resp;
		
		try {
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			Integer noticeFramework = productSetting.getNoticeFramework();
			
			if(noticeFramework != null && NoticeFrameworkConstant.findById(noticeFramework) == NoticeFrameworkConstant.XDOC) {
				LOG.debug("Using XDoc");
				resp = xdocAct.find(req);
			} else {
				//--: Jasper is default
				LOG.debug("Using Jasper");
				resp = jasperAct.find(req);
			}
		} catch (Exception e) {
			resp = new CommonCriteriaResp() {};
			resp.setStatusCode(1000);
			LOG.error(e.toString(), e);
		}
		
		LOG.debug(resp);
		LOG.debug("End");
		return resp;
	}
	
	@POST
	@Path("/download")
	public Response download(NoticeFindCriteriaReq req) throws Exception {
		try {
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			Integer noticeFramework = productSetting.getNoticeFramework();
			Response resp;
			
			if(noticeFramework != null && NoticeFrameworkConstant.findById(noticeFramework) == NoticeFrameworkConstant.XDOC) {
				LOG.debug("Using XDoc");
				resp = xdocAct.download(req);
			} else {
				//--: Jasper is default
				LOG.debug("Using Jasper");
				resp = jasperAct.download(req);
			}
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
}