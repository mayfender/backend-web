package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FIRST_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FULL_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_LAST_NAME;

import java.io.File;
import java.nio.file.Paths;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFRow;
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
import com.may.ple.backend.criteria.SmsCriteriaReq;
import com.may.ple.backend.criteria.SmsCriteriaResp;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class SmsService {
	private static final Logger LOG = Logger.getLogger(SmsService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	private UserAction userAct;
	@Value("${file.path.temp}")
	private String smsTemplatePath;
	
	@Autowired	
	public SmsService(MongoTemplate templateCore, DbFactory dbFactory, UserAction userAct) {
		this.templateCore = templateCore;
		this.dbFactory = dbFactory;
		this.userAct = userAct;
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
			fields.append("message", 1);		
			fields.append("messageField", 1);
			fields.append("createdDateTime", 1);
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
				
				for (Map<String, String> msgMap : productSetting.getSmsMessages()) {
					if(sms.get("messageField").equals(msgMap.get("fieldName"))) {
						sms.put("message", msgTransform(msgMap.get("fieldValue"), (Map)sms.get("taskDetailFull"), users));
						break;
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
	
	public String getTemplatePath() {
		return Paths.get(smsTemplatePath).getParent().toString() + File.separator + "smsTemplate";
	}
	
	public void sendSms(SmsCriteriaReq req) throws Exception {
		try {
			req.setItemsPerPage(1000);
			SmsCriteriaResp resp;
			int currentPage = 1;
			while(true) {
				req.setCurrentPage(currentPage++);
				resp = get(req, null);
				
//				LOG.info("Start call doSendSms");
//				doSendSms(resp.getSmses());
				
				if(req.getItemsPerPage() > resp.getSmses().size()) {				
					break;
				}
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void doSendSms(List<Map> req) throws Exception {
		try {
			org.jsoup.Connection.Response res;
			Map<String, String> result;
			Document doc;
			for (Map map : req) {		
				res = Jsoup.connect("http://www.thsms.com/api/rest")
						.timeout(30000)
						.method(Method.POST)
						.data("method", "send")
						.data("username", "plegibson")
						.data("password", "24ef83")
						.data("from", "SMS")
						.data("to", "0844358987")
						.data("message", "สวัสดีครับ ทุกคน my name is Mayfender.")
						.header("Content-Type", "application/x-www-form-urlencoded")
						.postDataCharset("UTF-8")
						.execute();
			
				doc = res.parse();
				
				result = new HashMap<String, String>();
				result.put("message", doc.select("message").html());
				result.put("uuid", doc.select("uuid").html());
				result.put("credit_usage", doc.select("credit_usage").html());
				result.put("credit", doc.select("credit").html());
				result.put("status", doc.select("status").html());
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

class HeaderHolderResp {
	public Map<String, HeaderHolder> header;
	public BasicDBObject fields;
	public XSSFRow rowCopy;
	public String delimiter;
	public String yearType;
	
	public HeaderHolderResp(Map<String, HeaderHolder> header, BasicDBObject fields, XSSFRow rowCopy, String delimiter, String yearType) {
		this.header = header;
		this.fields = fields;
		this.rowCopy = rowCopy;
		this.delimiter = delimiter;
		this.yearType = yearType;
	}
}
class HeaderHolder {
	public String type;
	public String format;
	public String emptySign;
	public int index;
}