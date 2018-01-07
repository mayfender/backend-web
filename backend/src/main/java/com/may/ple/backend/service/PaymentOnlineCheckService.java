package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_PAYMENT_DETAIL;
import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_IS_ACTIVE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_UPDATED_DATE_TIME;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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
import org.springframework.beans.factory.annotation.Value;
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
import com.may.ple.backend.bussiness.kys.KYSApi;
import com.may.ple.backend.bussiness.kys.LoginRespModel;
import com.may.ple.backend.bussiness.kys.StatusConstant;
import com.may.ple.backend.criteria.FileCommonCriteriaResp;
import com.may.ple.backend.criteria.PaymentOnlineChkCriteriaReq;
import com.may.ple.backend.entity.ApplicationSetting;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.PaymentOnlineUpdateModel;
import com.may.ple.backend.model.PaymentOnlineUpdateModel2;
import com.may.ple.backend.utils.MappingUtil;
import com.may.ple.backend.utils.PdfUtil;

@Service
public class PaymentOnlineCheckService {
	private static final Logger LOG = Logger.getLogger(PaymentOnlineCheckService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	private UserAction userAct;
	@Value("${file.path.temp}")
	private String filePathTemp;
	
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
				field.include("sys_lastPayDate");
				field.include("sys_lastPayAmount");
				field.include("sys_proxy");
			}
			
			List<Map> checkList = template.find(query, Map.class, NEW_TASK_DETAIL.getName());		
			resp.setCheckList(checkList);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	public void clearStatusChkLst(PaymentOnlineChkCriteriaReq req) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Update update = new Update();
			update.set("sys_status", 1);
			template.updateMulti(new Query(), update, NEW_TASK_DETAIL.getName());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
	public void updateChkLst(PaymentOnlineChkCriteriaReq req) throws Exception {
		try {
			Map<String, PaymentOnlineUpdateModel2> productMap = new HashMap<>();
			List<PaymentOnlineUpdateModel> updateList = req.getUpdateList();
			
			if(updateList == null) return;
			
			PaymentOnlineUpdateModel2 paymentModel;
			Map<String, Object> payment;
			List<String> ownerIds; 
			Map taskDetail;
			Update update;
			Query query;
			Field field;
			
			for (PaymentOnlineUpdateModel model : updateList) {
				if(!productMap.containsKey(model.getProductId())) {
					paymentModel = new PaymentOnlineUpdateModel2();
					paymentModel.template = dbFactory.getTemplates().get(model.getProductId());
					paymentModel.product = templateCenter.findOne(Query.query(Criteria.where("id").is(model.getProductId())), Product.class);
					paymentModel.headers = getAllColumnFormatsActive(paymentModel.product.getColumnFormats());
					paymentModel.users = userAct.getUserByProductToAssign(model.getProductId()).getUsers();
					
					productMap.put(model.getProductId(), paymentModel);
				} else {
					paymentModel = productMap.get(model.getProductId());
				}
				
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
					update.set("sys_proxy", model.getProxy());
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
					query = Query.query(Criteria.where(paymentModel.product.getProductSetting().getContractNoColumnName()).is(model.getContractNo()));
					field = query.fields();
					field.include(SYS_OWNER_ID.getName());
					for (ColumnFormat cf : paymentModel.headers) {
						field.include(cf.getColumnName());
					}
					
					taskDetail = paymentModel.template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
					if(taskDetail == null) continue;
					
					ownerIds = (List)taskDetail.get(SYS_OWNER_ID.getName());
					if(ownerIds != null || ownerIds.size() > 0) {
						List<Map<String, String>> userList = MappingUtil.matchUserId(paymentModel.users, ownerIds.get(0));
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
					
					payment.put(SYS_CREATED_DATE_TIME.getName(), model.getLastPayDate());
					payment.put(SYS_UPDATED_DATE_TIME.getName(), model.getLastPayDate());
					paymentModel.template.insert(payment, NEW_PAYMENT_DETAIL.getName());	
				}
				paymentModel.template.updateFirst(Query.query(Criteria.where("_id").is(model.getId())), update, NEW_TASK_DETAIL.getName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public FileCommonCriteriaResp getHtml(String id, String productId, boolean isReplaceUrl) throws Exception {
		try {
			FileCommonCriteriaResp resp = new FileCommonCriteriaResp();
			
			LOG.info("Start getHtml");
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			Query query = Query.query(Criteria.where("_id").is(id));
			query.fields()
			.include("ID_CARD")
			.include("BIRTH_DATE")
			.include("sys_uri")
			.include("sys_loanType")
			.include("sys_accNo")
			.include("sys_cif")
			.include("sys_proxy")
			.include("sys_sessionId");
			
			Map checkList = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
			Object sysUriObj = checkList.get("sys_uri");
			String html = "";
			String uriStr;
			
			if((sysUriObj = checkList.get("sys_uri")) != null) {
				uriStr = sysUriObj.toString();
				String loanType = checkList.get("sys_loanType").toString();
				String accNo = checkList.get("sys_accNo").toString();
				String cif = checkList.get("sys_cif").toString();
				String sessionId = checkList.get("sys_sessionId").toString();
				Proxy proxy = null;
				
				if(checkList.get("sys_proxy") != null) {
					String[] proxyStr = checkList.get("sys_proxy").toString().split(":");
					proxy = new Proxy(
							Proxy.Type.HTTP,
							InetSocketAddress.createUnresolved(proxyStr[0], Integer.parseInt(proxyStr[1]))
							);
				}
				
				LoginRespModel loginResp;
				Document doc;
				int round = 0;
				while(true) {
					doc = getPaymentInfoPage(proxy, uriStr, loanType, accNo, cif, sessionId);
					if(doc == null) {
						resp.setIsError(true);
						break;
					}
					
					Elements body = doc.select("body");
					String onload = body.get(0).attr("onload");
					
					if(StringUtils.isNoneBlank(onload) && onload.toLowerCase().contains("login")) {
						LOG.warn("Session Timeout");
						
						if(round == 1) {
							resp.setIsError(true);
							break;
						}
						
						LOG.debug("Call reLogin");
						loginResp = reLogin(proxy, checkList.get("ID_CARD").toString(), birthDateFormat(checkList.get("BIRTH_DATE").toString()));
						if(StatusConstant.SERVICE_UNAVAILABLE == loginResp.getStatus() || StatusConstant.LOGIN_FAIL == loginResp.getStatus()) {
							resp.setIsError(true);
							break;
						} else {
							sessionId = loginResp.getSessionId();
							round++;
							continue;
						}
					} else {
						Elements bExit = doc.select("td input[name='bExit']");
						if(bExit != null && bExit.size() > 0) {
							LOG.debug("Remove button");
							bExit.get(0).parent().remove();
						}
						
						LOG.info("Get HTML");
						html = doc.html();
						if(isReplaceUrl) {
							LOG.debug("Start replace absolute url");
							html = html.replaceAll("/STUDENT","https://www.e-studentloan.ktb.co.th/STUDENT");
						}
						LOG.debug("End getHtml");
						break;
					}
				}
			} else {
				resp.setIsError(true);
			}
			
			if(resp.getIsError() != null && resp.getIsError()) {
				resp.setHtml(errHtml());
			} else {				
				resp.setHtml(html);
			}
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public byte[] getHtml2Pdf(String productId, String id) throws Exception {
		try {
			FileCommonCriteriaResp resp = getHtml(id, productId, false);
			String html = resp.getHtml();
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			Query query = Query.query(Criteria.where("_id").is(id));
			Field field = query.fields();
			field.include("ลำดับ");
			field.include("OA_CODE");
			field.include("GROUP");
			Map taskDetail = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
			
			html = cleanHtml(html, taskDetail.get("ลำดับ").toString(), taskDetail.get("OA_CODE").toString(), taskDetail.get("GROUP").toString());
			String uuidDateTime = String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS%1$tL", Calendar.getInstance().getTime());
			
			query = new Query();
			query.fields().include("wkhtmltopdfPath");
			ApplicationSetting setting = templateCenter.findOne(query, ApplicationSetting.class);
			
			String pdfFile = filePathTemp + "/" + uuidDateTime + ".pdf";
			PdfUtil.html2pdf(setting.getWkhtmltopdfPath(),  html, pdfFile);
			byte[] data = FileUtils.readFileToByteArray(new File(pdfFile));
			
			FileUtils.deleteQuietly(new File(pdfFile));
			
			return data;
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
	
	private String errHtml() {
		return "<p><h4>ระบบไม่สามารถแสดงข้อมูลได้ กรุณาเช็คข้อมูลผ่าน <a href='https://www.e-studentloan.ktb.co.th/STUDENT/ESLLogin.do' target='_blank'>เว็บไซต์ กยศ.</a></h4></p>";
	}
	
	private LoginRespModel reLogin(Proxy proxy, String idCard, String birthDate) throws Exception {
		try {
			StatusConstant loginStatus = StatusConstant.LOGIN_FAIL;
			LoginRespModel loginResp = null;
			int errCount = 0;
			
			while(StatusConstant.LOGIN_FAIL == loginStatus || StatusConstant.SERVICE_UNAVAILABLE == loginStatus) {
				if(errCount == 10) break;
				
				loginResp = KYSApi.getInstance().login(proxy, idCard, birthDate, errCount);
				loginStatus = loginResp.getStatus();
				
				if(StatusConstant.SERVICE_UNAVAILABLE == loginStatus) {
					LOG.warn(" Service Unavailable");
					break;
				} else if(StatusConstant.LOGIN_FAIL  == loginStatus) {
					errCount++;
					Thread.sleep(1000);
				} else {
					LOG.info("Login Success");
				}
			}
			return loginResp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private Document getPaymentInfoPage(Proxy proxy, String uriStr, String loanType, String accNo, String cif, String sessionId) throws Exception {
		try {
			Response res;
			Document doc;
			int i = 0;
			
			while(true) {
				res = Jsoup.connect(uriStr)
						.proxy(proxy)
						.method(Method.POST)
						.data("loanType", loanType)
						.data("accNo", accNo)
						.data("cif", cif)
						.data("browser", "Fire Fox Or Other")
						.header("Content-Type", "application/x-www-form-urlencoded")
						.cookie("JSESSIONID", sessionId)
						.postDataCharset("UTF-8")
						.execute();
				
				doc = res.parse();
				
				if(doc.select("title").get(0).html().toUpperCase().equals("ERROR")) {
					if(i == 4) return null;
					
					LOG.error("Got error and try again round: " + i);
					Thread.sleep(1000);
					i++;
				} else {
					return doc;
				}
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private static String birthDateFormat(String str) {
		if(str.contains("/")) {
			return str;
		}
		
		String day = str.substring(0, 2);
		String month = str.substring(2, 4);
		String year = str.substring(4);
		return day + "/" + month + "/" + year;
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
