package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FIRST_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FULL_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_LAST_NAME;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.bussiness.ExcelReport;
import com.may.ple.backend.criteria.SmsCriteriaReq;
import com.may.ple.backend.criteria.SmsCriteriaResp;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.HeaderHolder;
import com.may.ple.backend.model.HeaderHolderResp;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class SmsService {
	public Map<String, Map> smsStatus = new HashMap<>();
	private static final Logger LOG = Logger.getLogger(SmsService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	private UserAction userAct;
	private ExcelReport excelUtil;
	@Value("${file.path.temp}")
	private String smsTemplatePath;
	
	@Autowired	
	public SmsService(MongoTemplate templateCore, DbFactory dbFactory, UserAction userAct, ExcelReport excelUtil) {
		this.templateCore = templateCore;
		this.dbFactory = dbFactory;
		this.userAct = userAct;
		this.excelUtil = excelUtil;
	}
	
	public void save(SmsCriteriaReq req) throws Exception {
		try {
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			List<ObjectId> ids = new ArrayList<>();
			
			for (String id : req.getIds()) {
				ids.add(new ObjectId(id));
			}
			
			Query query = Query.query(Criteria.where("_id").in(ids));
			Field fields = query.fields().include(SYS_OWNER_ID.getName());
			List<ColumnFormat> headers = product.getColumnFormats();
			headers = getColumnFormatsActive(headers);
			
			for (ColumnFormat colForm : headers) {
				fields.include(colForm.getColumnName());
			}
			
			List<Map> taskDetails = template.find(query, Map.class, NEW_TASK_DETAIL.getName());
			Calendar now = Calendar.getInstance();
			Update update;
			
			
			for (Map taskDetail : taskDetails) {
				if(taskDetail.get(productSetting.getContractNoColumnName()) == null) {
					throw new Exception("Not found contract_no column");
				}
					
				update = new Update();
				update.set("taskDetail", taskDetail);
				update.set("status", 0);
				update.set("createdDateTime", now.getTime());
				update.set("createdBy", new ObjectId(user.getId()));
				update.set("createdByName", user.getShowname());
				update.set("messageField", req.getMessageField());
				update.set("message", "");
				
				query = Query.query(Criteria.where("taskDetail." + productSetting.getContractNoColumnName())
						.is(taskDetail.get(productSetting.getContractNoColumnName()))
						.and("status").is(0));
						
				template.upsert(query, update, "sms");
			}
			
			LOG.debug("Check and create Index.");
			DBCollection collection = template.getCollection("sms");
			collection.createIndex(new BasicDBObject("status", 1));
			collection.createIndex(new BasicDBObject("messageField", 1));
			collection.createIndex(new BasicDBObject("createdDateTime", 1));
			
			for (ColumnFormat colForm : headers) {
				fields.include(colForm.getColumnName());
				collection.createIndex(new BasicDBObject("taskDetail." + colForm.getColumnName(), 1));
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public SmsCriteriaResp get(SmsCriteriaReq req, BasicDBObject fields) throws Exception {
		try {
			boolean isReport = fields != null ? true : false;
			
			SmsCriteriaResp resp = new SmsCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			List<ColumnFormat> headers = product.getColumnFormats();
			resp.setHeaders(getColumnFormatsActive(headers));
			
			String dateColumn;
			if(req.getStatus().intValue() == 0) {
				dateColumn = "createdDateTime";
			} else {
				dateColumn = "sentDateTime";
			}
			
			Criteria criteria = Criteria.where("status").is(req.getStatus());
			if(req.getDateFrom() != null) {
				if(req.getDateTo() != null) {
					criteria.and(dateColumn).gte(req.getDateFrom()).lte(req.getDateTo());			
				} else {
					criteria.and(dateColumn).gte(req.getDateFrom());
				}
			} else if(req.getDateTo() != null) {				
				criteria.and(dateColumn).lte(req.getDateTo());
			}
			
			MatchOperation match = Aggregation.match(criteria);
			Aggregation agg = Aggregation.newAggregation(			
					match,
					Aggregation.group().count().as("totalItems")	
			);

			AggregationResults<Map> aggregate = template.aggregate(agg, "sms", Map.class);
			Map aggCountResult = aggregate.getUniqueMappedResult();
			if(aggCountResult == null) {
				LOG.info("Not found data");
				resp.setTotalItems(Long.valueOf(0));
				return resp;
			}
			resp.setTotalItems(((Integer)aggCountResult.get("totalItems")).longValue());
			
			if(fields == null) {
				List<HeaderHolderResp> fieldFromMsg = getField(productSetting.getSmsMessages());
				if(fieldFromMsg == null || fieldFromMsg.size() == 0) {
					fields = new BasicDBObject();
				} else {
					fields = fieldFromMsg.get(0).fields;					
				}
				
				List<ColumnFormat> fieldMore = resp.getHeaders();
				
				for (ColumnFormat columnFormat : fieldMore) {
					if(fields.containsField("taskDetail." + columnFormat.getColumnName())) continue;
					fields.append("taskDetail." + columnFormat.getColumnName(), 1);
				}
				
				fields.append("status", 1).append("createdByName", 1);
			}
			fields.append("respMessage", 1);		
			fields.append("messageField", 1);
			fields.append("createdDateTime", 1);
			fields.append("createdBy", 1);
			fields.append("sentDateTime", 1);
			fields.append("message", 1);
			fields.append("taskDetail.sys_owner_id", 1);
			fields.append("taskDetailFull.sys_sms_number", 1);
			fields.append("taskDetailFull." + SYS_OWNER_ID.getName(), 1);
					
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("createdDateTime", -1));
			BasicDBObject project = new BasicDBObject("$project", fields);
			agg = Aggregation.newAggregation(
					match,
					new CustomAggregationOperation(sort),
					Aggregation.skip((req.getCurrentPage() - 1) * req.getItemsPerPage()),
					Aggregation.limit(req.getItemsPerPage()),
					new CustomAggregationOperation(
					        new BasicDBObject(
						            "$lookup",
						            new BasicDBObject("from", NEW_TASK_DETAIL.getName())
						                .append("localField", "taskDetail." + productSetting.getContractNoColumnName())
						                .append("foreignField", productSetting.getContractNoColumnName())
						                .append("as", "taskDetailFull")
					        	)),
					Aggregation.unwind("taskDetailFull"),
					new CustomAggregationOperation(project)
			);
			
			aggregate = template.aggregate(agg, "sms", Map.class);
			List<Map> smses = aggregate.getMappedResults();
			
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			List<Map<String, String>> userList;
			for (Map<String, Object> sms : smses) {
				userList = MappingUtil.matchUserId(users, ((List)(((Map)sms.get("taskDetail")).get("sys_owner_id"))).get(0).toString());
				((Map)sms.get("taskDetail")).put("sys_owner", userList == null ? "" : userList.get(0).get("showname"));
				
				if(isReport) continue;
				
				if(req.getStatus().intValue() != 1) {
					for (Map<String, String> msgMap : productSetting.getSmsMessages()) {
						if(sms.get("messageField").equals(msgMap.get("fieldName"))) {
							sms.put("message", msgTransform(msgMap.get("fieldValue"), (Map)sms.get("taskDetailFull"), users));
							break;
						}
					}
				}
			}
			
			resp.setSmses(smses);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void remove(SmsCriteriaReq req) throws Exception {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			List<ObjectId> ids = new ArrayList<>();
			
			for (String id : req.getIds()) {
				ids.add(new ObjectId(id));
			}
			
			template.remove(Query.query(Criteria.where("_id").in(ids)), "sms");			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void sendSms(final SmsCriteriaReq req) throws Exception {
		new Thread() {
			public void run() {
				LOG.info("Start thread to send SMS.");
				
				Map<String, Object> smsResult = new HashMap<>();
				smsResult.put("isFinished", false);
				smsResult.put("success", 0);
				smsResult.put("fail", 0);
				smsStatus.put(req.getProductId(), smsResult);
				
				try {	
					req.setItemsPerPage(1000);
					SmsCriteriaResp resp;
					int currentPage = 1;
					while(true) {
						req.setCurrentPage(currentPage++);
						resp = get(req, null);
						
						LOG.info("Start call doSendSms");
						doSendSms(resp.getSmses(), smsResult, req.getProductId());
						
						if(req.getItemsPerPage() > resp.getSmses().size()) {				
							break;
						}
					}
				} catch (Exception e) {
					LOG.error(e.toString(), e);
				} finally {
					smsResult.put("isFinished", true);
					LOG.info("End thread to send SMS.");
				}
			};
		}.start();
	}
	
	public Map getSmsSentStatus(String productId) {
		return smsStatus.get(productId);
	}
	
	public StreamingOutput report(final SmsCriteriaReq req, final String filePath) {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				OutputStream out = null;
				ByteArrayInputStream in = null;
				FileInputStream fis = null;
				XSSFWorkbook workbook = null;
				
				try {
					out = new BufferedOutputStream(os);
					fis = new FileInputStream(new File(filePath));
					workbook = new XSSFWorkbook(fis);
					fis.close();
					
					XSSFSheet sheet = workbook.getSheetAt(0);
					
					HeaderHolderResp headerHolderResp = excelUtil.getHeader(sheet).get(0);
					List<Map> allData = new ArrayList<>();
					req.setItemsPerPage(1000);
					int currentPage = 1;
					SmsCriteriaResp resp;
					
					LOG.info("Start get traceResult");
					while(true) {
						req.setCurrentPage(currentPage++);
						resp = get(req, headerHolderResp.fields);
						allData.addAll(resp.getSmses());
						
						if(req.getItemsPerPage() > resp.getSmses().size()) {				
							break;
						}
					}
					
					//--[.]
					excelUtil.fillBody(
							headerHolderResp,
							sheet,
							allData,
							req.getProductId(),
							false,
							null,
							null
							);
					
					//--[* Have to placed before write out]
					XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
					workbook.write(out);
				} catch (Exception e) {
					LOG.error(e.toString(), e);
				} finally {
					try {if(workbook != null) workbook.close();} catch (Exception e2) {}
					try {if(fis != null) fis.close();} catch (Exception e2) {}
					try {if(in != null) in.close();} catch (Exception e2) {}
					try {if(out != null) out.close();} catch (Exception e2) {}
				}
			}
		};
	}
	
	private void doSendSms(List<Map> req, Map<String, Object> smsResult, String productId) throws Exception {
		try {
			Product product = templateCore.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
			ProductSetting productSetting = product.getProductSetting();
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			Date now = Calendar.getInstance().getTime();
			int credit, creditUsage, statusCode;
			Map<String, Object> taskDetailFull;
			org.jsoup.Connection.Response res;
			String status = "fail", respMsg, uuid = "";
			Update update;
			Document doc;
			
			for (Map map : req) {
				taskDetailFull = (Map)map.get("taskDetailFull");
				
				if(taskDetailFull == null) {					
					respMsg = "ไม่พบข้อมูลลูกค้า";
				} else if(taskDetailFull.get("errCode") != null && taskDetailFull.get("errCode").equals(1)) {
					respMsg = "ข้อมูลไม่สมบูรณ์";			
				} else if(taskDetailFull.get("sys_sms_number") == null || StringUtils.isBlank((String)taskDetailFull.get("sys_sms_number"))) {
					respMsg = "ไม่พบเบอร์โทรที่ใช้ส่ง SMS";
				} else {
					res = Jsoup.connect("http://www.thsms.com/api/rest")
							.timeout(30000)
							.method(Method.POST)
							.data("method", "send")
							.data("username", productSetting.getSmsUsername())
							.data("password", productSetting.getSmsPassword())
							.data("from", productSetting.getSmsSenderName())
							.data("to", taskDetailFull.get("sys_sms_number").toString())
							.data("message", map.get("message").toString())
							.header("Content-Type", "application/x-www-form-urlencoded")
							.postDataCharset("UTF-8")
							.execute();
				
					doc = res.parse();
					status = doc.select("status").html();
					respMsg = doc.select("message").html();
					uuid = doc.select("uuid").html();
					
					credit = Integer.parseInt(StringUtils.isBlank(doc.select("credit").html()) ? "0" : doc.select("credit").html());
					creditUsage = Integer.parseInt(StringUtils.isBlank(doc.select("credit_usage").html()) ? "0" : doc.select("credit_usage").html());
					
					smsResult.put("credit", credit);
					smsResult.put("creditUsage", creditUsage);
					
					if(credit == 0) {
						LOG.error("##### No credit to use.");
						break;
					}
				}
				
				if(status.equals("success")) {
					smsResult.put("success", (int)smsResult.get("success") + 1);
					statusCode = 1;
				} else {
					smsResult.put("fail", (int)smsResult.get("fail") + 1);
					statusCode = 2;
				}
				
				update = Update.update("respMessage", respMsg);
				update.set("message", map.get("message").toString());
				update.set("uuid", uuid);
				update.set("status", statusCode);
				update.set("sentDateTime", now);
				update.set("taskDetail.sys_sms_number", taskDetailFull.get("sys_sms_number").toString());
				
				template.updateFirst(Query.query(Criteria.where("_id").is(map.get("_id"))), update, "sms");
			}
			
			Date end = Calendar.getInstance().getTime();
			long diff = end.getTime() - now.getTime();
			
			if(diff < 5000) {
				LOG.info("Wait...");
				Thread.sleep(5000);				
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private List<HeaderHolderResp> getField(List<Map> sources) {
		try {
			List<String> msgs = new ArrayList<>();
			String msg;
			Pattern r;
			Matcher m;
			for (Map map : sources) {
				msg = map.get("fieldValue").toString();
				r = Pattern.compile("\\$\\{([^}]+)\\}");
				m = r.matcher(msg);
				
				while(m.find()) {
					msgs.add(m.group());
				}
			}
			return getMsgFormat(msgs);
		} catch (Exception e) {
			throw e;
		}
	}
	
	private String msgTransform(String source, Map data, List<Users> users) {
		try {
			Date now = Calendar.getInstance().getTime();
			StringBuffer result = new StringBuffer(source.length());
			Pattern r = Pattern.compile("\\$\\{([^}]+)\\}");
			String firstName = "", lastName = "";
			List<Map<String, String>> userList;
			Matcher m = r.matcher(source);
			String resultMsg = "";
			HeaderHolderResp header;
			List<String> msgList;
			List<String> ownerId;
			HeaderHolder holder;
			Set<String> keySet;
			Object val;
			Map u;
			
			while(m.find()) {
				msgList = new ArrayList<>();
				msgList.add(m.group());
				header = getMsgFormat(msgList).get(0);
				keySet = header.fields.keySet();
				
				for (String key : keySet) {
					if(!data.containsKey("createdDateTime")) {
						if(header.yearType != null && header.yearType.equals("BE")) {								
							data.put("createdDateTime", new SimpleDateFormat("dd/MM/yyyy", new Locale("th", "TH")).format(now));
						} else {
							data.put("createdDateTime", new SimpleDateFormat("dd/MM/yyyy", new Locale("en", "US")).format(now));
						}
					}
					
					if(!data.containsKey(SYS_OWNER_FULL_NAME.getName())) {
						ownerId = (List)data.get(SYS_OWNER_ID.getName());
						if(ownerId != null && ownerId.size() > 0) {
							userList = MappingUtil.matchUserId(users, ownerId.get(0));
							if(userList != null && userList.size() > 0) {
								u = (Map)userList.get(0);				
								firstName = "";
								lastName = "";
								
								if(u.get("firstName") != null) {							
									firstName = u.get("firstName").toString();
									data.put(SYS_OWNER_FIRST_NAME.getName(), firstName);
								}
								if(u.get("lastName") != null) {		
									lastName = u.get("lastName").toString();
									data.put(SYS_OWNER_LAST_NAME.getName(), lastName);
								}
								data.put(SYS_OWNER_FULL_NAME.getName(), (StringUtils.trimToEmpty(firstName) + " " + StringUtils.trimToEmpty(lastName)).trim());
								data.put("owner_tel", u.get("phone"));
							}
						} else {
							data.put(SYS_OWNER_FULL_NAME.getName(), "");
						}
					}
					
					holder = header.header.get(key);
					val = data.get(key.replace("taskDetailFull.", ""));
					
					if(val == null) {
						data.put("errCode", 1);
						continue;
					}
					if(val instanceof String) {
						if(StringUtils.isBlank(String.valueOf(val))) {
							data.put("errCode", 1);							
							continue;
						}
					}
					
					if(holder.type != null && holder.type.contains("date")) {	
						if(header.yearType != null && header.yearType.equals("BE")) {								
							resultMsg = new SimpleDateFormat(holder.format, new Locale("th", "TH")).format(val);
						} else {
							resultMsg = new SimpleDateFormat(holder.format, new Locale("en", "US")).format(val);
						}
					} else if(holder.type != null && holder.type.equals("num")) {
						resultMsg = String.format("%1$,.2f", val);
					} else {
						resultMsg = val.toString();
					}
					
					m.appendReplacement(result, Matcher.quoteReplacement(resultMsg));					
					break;
				}
			}
			
			m.appendTail(result);
			
			return result.toString();
		} catch (Exception e) {
			throw e;
		}
	}
	
	private List<HeaderHolderResp> getMsgFormat(List<String> sources) {
		try {
			List<HeaderHolderResp> result = new ArrayList<>();
			Map<String, HeaderHolder> header = new LinkedHashMap<>();
			BasicDBObject fields = new BasicDBObject();
			String delimiter = null, yearType = null;
			String[] headers, delimiters, yearTypes;
			HeaderHolder headerHolder;
			
			for (String source : sources) {
				if(source.startsWith("${")) {
					source = source.replace("${", "").replace("}", "");
					headers = source.split("&");
					
					headerHolder = new HeaderHolder();
					source = headers[0];
					
					if(source.contains("#")) {
						delimiters = source.split("#");
						source = delimiters[0];				
						yearTypes = delimiters[1].split("\\^");
						delimiter = yearTypes[0];
						yearType = yearTypes[1];
					}
					
					if(headers.length > 1) {
						headerHolder.type = headers[1];
						
						if(headers.length > 2) {
							headerHolder.format = headers[2];
							
							if(headers.length > 3) {
								headerHolder.emptySign = headers[3];
							}
						}
					}
					
					fields.append(source.equals("createdDate") || source.equals("createdTime") ? "createdDateTime" : source, 1);
					
					if(header.containsKey(source)) {							
						header.put(source + "_" + headerHolder.index, headerHolder);
					} else {							
						header.put(source, headerHolder);
					}
				}						
			}
			
			if(header.size() > 0) {				
				result.add(new HeaderHolderResp(header, fields, null, delimiter, yearType));
			}
			
			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private List<ColumnFormat> getColumnFormatsActive(List<ColumnFormat> columnFormats) {
		List<ColumnFormat> result = new ArrayList<>();
		
		for (ColumnFormat colFormat : columnFormats) {
			if(colFormat.getIsActive()) {
				result.add(colFormat);
			}
		}
		
		return result;
	}

	public UserAction getUserAct() {
		return userAct;
	}
	
}