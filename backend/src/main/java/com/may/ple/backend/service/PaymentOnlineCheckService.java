package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_PAYMENT_DETAIL;
import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_IS_ACTIVE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_UPDATED_DATE_TIME;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.ibm.icu.util.Calendar;
import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.criteria.FileCommonCriteriaResp;
import com.may.ple.backend.criteria.PaymentOnlineChkCriteriaReq;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.PaymentOnlineUpdateModel;
import com.may.ple.backend.utils.MappingUtil;

@Service
public class PaymentOnlineCheckService {
	private static final Logger LOG = Logger.getLogger(PaymentOnlineCheckService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	private UserAction userAct;
	
	@Autowired
	public PaymentOnlineCheckService(DbFactory dbFactory, MongoTemplate templateCenter, UserAction userAct) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
		this.userAct = userAct;
	}
	
	public FileCommonCriteriaResp getCheckListShow(PaymentOnlineChkCriteriaReq req) throws Exception {
		try {
			FileCommonCriteriaResp resp = new FileCommonCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting setting = product.getProductSetting();
			
			List<ColumnFormat> headers = product.getColumnFormats();
			headers = getColumnFormatsActive(headers);
			resp.setHeaders(headers);
			
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			
			Criteria criteria = Criteria.where(SYS_IS_ACTIVE.getName() + ".status").is(true).and("sys_status").is(2);
			long totalItems = template.count(Query.query(criteria), NEW_TASK_DETAIL.getName());
			
			if(totalItems == 0) {
				LOG.info("Not found data");
				criteria = Criteria.where(SYS_IS_ACTIVE.getName() + ".status").is(true).and("sys_status").is(3);
				totalItems = template.count(Query.query(criteria), NEW_TASK_DETAIL.getName());
				resp.setTotalItems(totalItems);
			} else {				
				resp.setTotalItems(totalItems);
			}
			
			LOG.debug("End count");
			if(totalItems == 0) return resp;
			
			LOG.debug("Start get data");
			Query query = Query.query(criteria).with(new PageRequest(0, 10));
			query.with(new Sort(Direction.DESC, SYS_UPDATED_DATE_TIME.getName()));
			Field field = query.fields()
			.include(SYS_UPDATED_DATE_TIME.getName())
			.include("sys_paidDateTime")
			.include("sys_status")
			.include("sys_sessionId")
			.include("sys_cif")
			.include(SYS_OWNER_ID.getName())
			.include(setting.getIdCardNoColumnName())
			.include(setting.getBirthDateColumnName());
			
			for (ColumnFormat columnFormat : headers) {
				field.include(columnFormat.getColumnName());				
			}
			
			List<Map> checkList = template.find(query, Map.class, NEW_TASK_DETAIL.getName());		
			Map<String, List<Map>> checkListGroup = groupByStatus(checkList, users, req, true);
			
			resp.setCheckMapList(checkListGroup);
			LOG.debug("End get data");
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public FileCommonCriteriaResp getCheckList(PaymentOnlineChkCriteriaReq req) throws Exception {
		try {
			FileCommonCriteriaResp resp = new FileCommonCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting setting = product.getProductSetting();
			resp.setContractNoColumnName(setting.getContractNoColumnName());
			resp.setIdCardNoColumnName(setting.getIdCardNoColumnName());
			resp.setBirthDateColumnName(setting.getBirthDateColumnName());
			
			Criteria criteria = null;
			if(req.getWorkType().equals("LOGIN")) {
				criteria = Criteria.where(SYS_IS_ACTIVE.getName() + ".status").is(true).and("sys_status").ne(3);
			} else {
				criteria = Criteria.where(SYS_IS_ACTIVE.getName() + ".status").is(true).and("sys_status").is(3);
			}
			
			long totalItems = template.count(Query.query(criteria), NEW_TASK_DETAIL.getName());
			resp.setTotalItems(totalItems);
			
			Query query = Query.query(criteria).with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			Field field = query.fields();
			
			if(req.getWorkType().equals("LOGIN")) {
				field.include(SYS_UPDATED_DATE_TIME.getName());
				field.include("sys_paidDateTime");
				field.include("sys_status");
				field.include("sys_sessionId");
				field.include("sys_cif");
				field.include(setting.getIdCardNoColumnName());
				field.include(setting.getBirthDateColumnName());
			} else {
				field.include(setting.getContractNoColumnName());
				field.include(SYS_UPDATED_DATE_TIME.getName());
				field.include("sys_sessionId");
				field.include("sys_cif");
				field.include("sys_loanType");
				field.include("sys_accNo");
				field.include("sys_uri");
				field.include("sys_totalPayInstallment");
				field.include("sys_preBalance");
				field.include("sys_lastPayAmount");
			}
			
			List<Map> checkList = template.find(query, Map.class, NEW_TASK_DETAIL.getName());		
			resp.setCheckList(checkList);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateChkLst(PaymentOnlineChkCriteriaReq req) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting setting = product.getProductSetting();
			List<ColumnFormat> headers = product.getColumnFormats();
			headers = getAllColumnFormatsActive(headers);
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			
			List<PaymentOnlineUpdateModel> updateList = req.getUpdateList();
			if(updateList == null) return;
			
			Date now = Calendar.getInstance().getTime();
			Map<String, Object> payment;
			List<String> ownerIds; 
			Map taskDetail;
			Update update;
			Query query;
			Field field;
			for (PaymentOnlineUpdateModel model : updateList) {
				update = new Update();
				update.set(SYS_UPDATED_DATE_TIME.getName(), model.getCreatedDateTime());
				
				if(model.getStatus() == 2) {
					//---[Login Error]
					update.set("sys_status", model.getStatus());
					update.set("sys_errMsg", model.getErrMsg());
				} else if(model.getStatus() == 3) {
					//---[Login Success]
					update.set("sys_status", model.getStatus());
					update.set("sys_sessionId", model.getSessionId());					
					update.set("sys_cif", model.getCif());
					update.set("sys_loanType", model.getLoanType());
					update.set("sys_accNo", model.getAccNo());
					update.set("sys_flag", model.getFlag());
					update.set("sys_uri", model.getUri());
				} else if(model.getStatus() == 4) {
					//---[Update Check Payment Timestamp]
				} else if(model.getStatus() == 5) {
					//---[Update Paid data]
					update.set("sys_lastPayDate", model.getLastPayDate());
					update.set("sys_lastPayAmount", model.getLastPayAmount());
					update.set("sys_totalPayInstallment", model.getTotalPayInstallment());
					update.set("sys_preBalance", model.getPreBalance());
					
					payment = new LinkedHashMap<>();
					payment.put("contract_no", model.getContractNo());
					payment.put("pay_date", model.getLastPayDate());
					payment.put("pay_amount", model.getLastPayAmount());
					
					LOG.info("Insert payment data");
					query = Query.query(Criteria.where(setting.getContractNoColumnName()).is(model.getContractNo()));
					field = query.fields();
					field.include(SYS_OWNER_ID.getName());
					for (ColumnFormat cf : headers) {
						field.include(cf.getColumnName());
					}
					
					taskDetail = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
					if(taskDetail == null) continue;
					
					ownerIds = (List)taskDetail.get(SYS_OWNER_ID.getName());
					if(ownerIds != null || ownerIds.size() > 0) {
						List<Map<String, String>> userList = MappingUtil.matchUserId(users, ownerIds.get(0));
						Map u = (Map)userList.get(0);
						
						payment.put(SYS_OWNER_ID.getName(), ownerIds.get(0));
						taskDetail.put(SYS_OWNER.getName(), u.get("showname"));
						payment.put("taskDetail", taskDetail);
					}
					
					payment.put("html", cleanHtml(model.getHtml(), 
										taskDetail.get("ลำดับ").toString(), 
										taskDetail.get("OA_CODE").toString(), 
										taskDetail.get("GROUP").toString())
					);
					
					payment.put(SYS_CREATED_DATE_TIME.getName(), now);
					payment.put(SYS_UPDATED_DATE_TIME.getName(), now);
					template.insert(payment, NEW_PAYMENT_DETAIL.getName());	
				}
				template.updateFirst(Query.query(Criteria.where("_id").is(model.getId())), update, NEW_TASK_DETAIL.getName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String getHtml(String id, String productId) throws Exception {
		try {
			LOG.info("Start getHtml");
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			Query query = Query.query(Criteria.where("_id").is(id));
			query.fields()
			.include("sys_uri")
			.include("sys_loanType")
			.include("sys_accNo")
			.include("sys_cif")
			.include("sys_sessionId");
			
			Map checkList = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
			Object sysUriObj = checkList.get("sys_uri");
			String uriStr;
			String html;
			
			if((sysUriObj = checkList.get("sys_uri")) != null) {
				uriStr = sysUriObj.toString();
				String loanType = checkList.get("sys_loanType").toString();
				String accNo = checkList.get("sys_accNo").toString();
				String cif = checkList.get("sys_cif").toString();
				String sessionId = checkList.get("sys_sessionId").toString();
				
				Response res = Jsoup.connect(uriStr)
						.method(Method.POST)
						.data("loanType", loanType)
						.data("accNo", accNo)
						.data("cif", cif)
						.data("browser", "Fire Fox Or Other")
						.header("Content-Type", "application/x-www-form-urlencoded")
						.cookie("JSESSIONID", sessionId)
						.postDataCharset("UTF-8")
						.execute();
				
				Document doc = res.parse();
				
				Elements body = doc.select("body");
				String onload = body.get(0).attr("onload");
				
				if(StringUtils.isNoneBlank(onload) && onload.toLowerCase().contains("login")) {
					PaymentOnlineUpdateModel updateModel = new PaymentOnlineUpdateModel();
					updateModel.setId(id);
					updateModel.setStatus(2);
					updateModel.setErrMsg("User Check Payment Fail");
					
					PaymentOnlineChkCriteriaReq req = new PaymentOnlineChkCriteriaReq();
					List<PaymentOnlineUpdateModel> updateLst = new ArrayList<>();					
					updateLst.add(updateModel);
					req.setProductId(productId);
					req.setUpdateList(updateLst);
					updateChkLst(req);
					html = errHtml();
				} else {
					LOG.info("Remove button");
					Elements bExit = doc.select("td input[name='bExit']");
					bExit.get(0).parent().remove();
					
					LOG.debug("Start replace absolute url");
					html = doc.html().replaceAll("/STUDENT","https://www.e-studentloan.ktb.co.th/STUDENT");
					LOG.info("End getHtml");
				}
			} else {
				html = errHtml();
			}
			return html;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private Map<String, List<Map>> groupByStatus(List<Map> checkList, List<Users> users, PaymentOnlineChkCriteriaReq req, boolean isIncludeUser) {
		try {
			Map<String, List<Map>> checkListGroup = new HashMap<>();
			List<Map<String, String>> userList;
			List<String> userIds;
			List<Map> data;
			String uId;
			
			for (Map map : checkList) {
				if(isIncludeUser) {
					userIds = (List)map.get(SYS_OWNER_ID.getName());
					
					if(userIds == null) continue;
					
					uId = userIds.get(0);
					
					if(StringUtils.isNoneBlank(req.getOwner()) && !req.getOwner().equals(uId)) {
						continue;
					}
					
					if(users != null) {						
						userList = MappingUtil.matchUserId(users, uId);
						map.put(SYS_OWNER.getName(), userList);
					}
				}
				
				if(checkListGroup.containsKey(map.get("sys_status").toString())) {
					checkListGroup.get(map.get("sys_status").toString()).add(map);
				} else {
					data = new ArrayList<>();
					data.add(map);
					checkListGroup.put(map.get("sys_status").toString(), data);					
				}
			}
			return checkListGroup;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private List<ColumnFormat> getColumnFormatsActive(List<ColumnFormat> columnFormats) {
		List<ColumnFormat> result = new ArrayList<>();
		int i = 0;
		for (ColumnFormat colFormat : columnFormats) {
			if(i == 5) break;
			
			if(colFormat.getIsActive()) {
				result.add(colFormat);
				i++;
			}
		}
		
		return result;
	}
	
	private List<ColumnFormat> getAllColumnFormatsActive(List<ColumnFormat> columnFormats) {
		List<ColumnFormat> result = new ArrayList<>();
		
		for (ColumnFormat colFormat : columnFormats) {
			if(colFormat.getIsActive()) {
				result.add(colFormat);
			}
		}
		
		return result;
	}
	
	private String errHtml() {
		return "<p><h4>ระบบไม่สามารถแสดงข้อมูลได้ กรุณาเช็คข้อมูลผ่าน <a href='https://www.e-studentloan.ktb.co.th/STUDENT/ESLLogin.do' target='_blank'>เว็บไซต์ กยศ.</a></h4></p>";
	}
	
	private String cleanHtml(String htlm, String index, String oaCode, String group) {
		try {
			String htmlInsert = ""
					+ "<font size=\"4\">"
					+ "รหัสบริษัท : " + oaCode + "<br>"
					+ "กลุ่มงาน : " + group + "<br>"
					+ "เลขจัดสรร : " + index +"<br>"
					+ "</font>";
					
			String html = htlm.replace("TIS-620", "UTF-8");
			Document doc = Jsoup.parse(html, "", Parser.htmlParser());
			doc.select("script").remove();
			doc.select("#tab2").remove();
			doc.select("#tab3").remove();
			doc.select("#tab4").remove();
			doc.select("input[type='hidden']").remove();
			
			if(doc.select("input[name='bExit']").size() > 0) {
				doc.select("input[name='bExit']").get(0).parent().remove();							
			}
			
			doc.select("head").first().html("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
			doc.select("body").first().prepend(htmlInsert);
			Elements tr = doc.select("table > tbody > tr");
			if(tr != null && tr.size() > 0) {
				Element trFirst = tr.first();
				Elements td = trFirst.select("td");
				if(td != null && td.size() > 0) {
					td.get(0).remove();
					td.get(1).remove();
				}
			}
			
			Elements div = doc.select(".thDash div");
			if(div != null && div.size() > 0) {
				Element element = div.get(0);
				element.html("&nbsp;" + element.html());
			}
			
			return doc.html();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	/*public static void main(String[] args) throws IOException {
		Document doc = Jsoup.parse(new File("C:/Users/mayfender/Desktop/test.html"), "utf-8");
		doc.select("head").first().html("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		Elements tr = doc.select("table > tbody > tr");
		
		if(tr != null && tr.size() > 0) {
			Element trFirst = tr.first();
			Elements td = trFirst.select("td");
			if(td != null && td.size() > 0) {
				td.get(0).remove();
				td.get(1).remove();
			}
		}
		
		Element element = doc.select(".thDash div").get(0);
		element.html("&nbsp;" + element.html());
		
		System.out.println(doc.html());
	}*/
	
}
