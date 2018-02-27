package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_PAYMENT_DETAIL;
import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_IS_ACTIVE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_UPDATED_DATE_TIME;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.icu.util.Calendar;
import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.constant.PluginModuleConstant;
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
	private SettingService settingServ;
	
	@Autowired
	public PaymentOnlineCheckService(DbFactory dbFactory, MongoTemplate templateCenter, UserAction userAct, SettingService settingServ) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
		this.userAct = userAct;
		this.settingServ = settingServ;
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
				field.include("sys_proxy");
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
					update.set("sys_proxy", model.getProxy());
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
		FileCommonCriteriaResp resp = new FileCommonCriteriaResp();
		Map checkList = null;
		
		try {			
			LOG.info("Start getHtml id: " + id);
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			Query query = Query.query(Criteria.where("_id").is(id));
			query.fields()
			.include("ID_CARD")
			.include("BIRTH_DATE")
			.include("sys_uri")
			.include("sys_loanType")
			.include("sys_accNo")
			.include("sys_cif")
			.include("sys_flag")
			.include("sys_proxy")
			.include("sys_sessionId");
			
			checkList = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
			
			JsonObject jsonWrite = new JsonObject();
			jsonWrite.addProperty("accNo", checkList.get("sys_accNo") == null ? "" : checkList.get("sys_accNo").toString());
			jsonWrite.addProperty("loanType", checkList.get("sys_loanType") == null ? "" : checkList.get("sys_loanType").toString());
			jsonWrite.addProperty("cif", checkList.get("sys_cif") == null ? "" : checkList.get("sys_cif").toString());
			jsonWrite.addProperty("uri", checkList.get("sys_uri") == null ? "" : checkList.get("sys_uri").toString());
			jsonWrite.addProperty("sessionId", checkList.get("sys_sessionId") == null ? "" : checkList.get("sys_sessionId").toString());
			jsonWrite.addProperty("proxy", checkList.get("sys_proxy") == null ? "" : checkList.get("sys_proxy").toString());
			jsonWrite.addProperty("ID_CARD", checkList.get("ID_CARD").toString());
			jsonWrite.addProperty("BIRTH_DATE", checkList.get("BIRTH_DATE").toString());
				
			PrintWriter writer = null;
			BufferedReader reader = null;
			Socket socket = null;
			
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress(settingServ.getChkPayIP() == null ? "127.0.0.1" : settingServ.getChkPayIP(), PluginModuleConstant.KYS.getPort()), 5000);
				
				writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);					
				writer.println(jsonWrite.toString());
				
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				JsonElement jsonElement =  new JsonParser().parse(reader.readLine());
				JsonObject jsonRead = jsonElement.getAsJsonObject();
				String html = jsonRead.get("html").getAsString();
				String sessionId = jsonRead.get("sessionId") == null ? "" : jsonRead.get("sessionId").getAsString();
				boolean isErr = jsonRead.get("isErr").getAsBoolean();
				
				if(StringUtils.isNotBlank(sessionId)) {
					LOG.info("Relogin and update session");
					PaymentOnlineChkCriteriaReq req = new PaymentOnlineChkCriteriaReq();
					List<PaymentOnlineUpdateModel> updateList = new ArrayList<>();
					
					PaymentOnlineUpdateModel paymentModel = new PaymentOnlineUpdateModel();
					paymentModel.setProductId(productId);
					paymentModel.setId(id);
					paymentModel.setCreatedDateTime(Calendar.getInstance().getTime());
					paymentModel.setStatus(3);
					paymentModel.setSessionId(sessionId);
					paymentModel.setUri(jsonRead.get("uri").getAsString());						
					paymentModel.setLoanType(jsonRead.get("loanType").getAsString());
					paymentModel.setFlag(jsonRead.get("flag").getAsString());
					paymentModel.setAccNo(jsonRead.get("accNo").getAsString());
					paymentModel.setCif(jsonRead.get("cif").getAsString());
					paymentModel.setProxy(checkList.get("sys_proxy") == null ? null : checkList.get("sys_proxy").toString());
					
					updateList.add(paymentModel);
					req.setUpdateList(updateList);
					
					updateChkLst(req);
				}
				
				if(isReplaceUrl && !isErr) {
					LOG.debug("Start replace absolute url");
					html = html.replaceAll("/STUDENT","https://www.e-studentloan.ktb.co.th/STUDENT");
				}
				
				resp.setIsError(isErr);
				resp.setHtml(isErr ? errHtml() : html);
			} catch (Exception e) {
				LOG.error(e.toString());
				throw e;
			} finally {
				if(reader != null) reader.close();
				if(socket != null) socket.close();
			}
		} catch (Exception e) {
			try {
				if(checkList != null) LOG.error("[" + checkList.get("ID_CARD") + "] " + e.toString(), e);
				else LOG.error(e.toString(), e);
			} catch (Exception e2) {
				LOG.error(e2.toString(), e2);
			}
			resp.setIsError(true);
			resp.setHtml(errHtml());
		}
		return resp;
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
	
	private String cleanHtml(String html, String index, String oaCode, String group) {
		Document doc = null;
		
		try {
			String htmlInsert = ""
					+ "<font size=\"4\">"
					+ "รหัสบริษัท : " + oaCode + "<br>"
					+ "กลุ่มงาน : " + group + "<br>"
					+ "เลขจัดสรร : " + index +"<br>"
					+ "</font>";
					
			html = html.replace("TIS-620", "UTF-8");
			doc = Jsoup.parse(html, "", Parser.htmlParser());
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
				if(td != null && td.size() > 1) {
					td.get(0).remove();
					td.get(1).remove();
				}
			}
			
			Elements div = doc.select(".thDash div");
			if(div != null && div.size() > 0) {
				Element element = div.get(0);
				element.html("&nbsp;" + element.html());
			}
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
		return doc == null ? html : doc.html();			
	}
	
	private String errHtml() {
		return "<p><h4>ระบบไม่สามารถแสดงข้อมูลได้ กรุณาเช็คข้อมูลผ่าน <a href='https://www.e-studentloan.ktb.co.th/STUDENT/ESLLogin.do' target='_blank'>เว็บไซต์ กยศ.</a></h4></p>";
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
