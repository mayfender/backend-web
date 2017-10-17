package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_PAYMENT_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_PROBATION_OWNER_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.constant.SysFieldConstant;
import com.may.ple.backend.criteria.PaymentDetailCriteriaReq;
import com.may.ple.backend.criteria.PaymentDetailCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;

@Service
public class PaymentDetailService {
	private static final Logger LOG = Logger.getLogger(PaymentDetailService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	private UserAction userAct;
	private UserService userService;
	
	@Autowired
	public PaymentDetailService(DbFactory dbFactory, MongoTemplate templateCenter, UserAction userAct, UserService userService) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
		this.userAct = userAct;
		this.userService = userService;
	}
	
	public PaymentDetailCriteriaResp find(PaymentDetailCriteriaReq req) throws Exception {
		try {
			LOG.debug("Start find");
			PaymentDetailCriteriaResp resp = new PaymentDetailCriteriaResp();
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			List<ColumnFormat> columnFormatsPayment = product.getColumnFormatsPayment();
			List<ColumnFormat> taskDetailHeaders = product.getColumnFormats();
			ProductSetting productSetting = product.getProductSetting();
			String contractNoColumn = productSetting.getContractNoColumnNamePayment();				
			String sortingColPayment = productSetting.getSortingColumnNamePayment();
			String paydateColName = productSetting.getPaidDateColumnNamePayment();
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			taskDetailHeaders = getColumnFormatsActive(taskDetailHeaders);
			resp.setUsers(users);
			
			List<String> probationUserIds = new ArrayList<>();
			for (Users u : users) {
				if(u.getProbation() == null || !u.getProbation()) continue;
				probationUserIds.add(u.getId());
			}
			
			if(columnFormatsPayment == null) return resp;
			
			LOG.debug("Before size: " + columnFormatsPayment.size());
			columnFormatsPayment = getColumnFormatsActive(columnFormatsPayment);
			LOG.debug("After size: " + columnFormatsPayment.size());
			
			//-------------------------------------------------------------------------------------
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Criteria criteria = new Criteria();
			
			if(!StringUtils.isBlank(req.getFileId())) {				
				criteria.and(SYS_FILE_ID.getName()).is(req.getFileId());
			}
			if(!StringUtils.isBlank(req.getContractNo())) {
				criteria.and(contractNoColumn).is(req.getContractNo());
			}
			if(!StringUtils.isBlank(req.getOwner())) {
				Users user = userService.getUserById(req.getOwner());
				Boolean probation = user.getProbation();
				if(probation != null && probation) {
					criteria.and("taskDetail." + SYS_PROBATION_OWNER_ID.getName()).is(req.getOwner());
				} else {
					if(probationUserIds.size() > 0) {
						criteria.and("taskDetail." + SYS_PROBATION_OWNER_ID.getName()).nin(probationUserIds);
					}
					criteria.and("taskDetail." + SYS_OWNER_ID.getName() + ".0").is(req.getOwner());										
				}
			}
			
			//-------------------------------------------------------------------------------------
			Query query = Query.query(criteria);
			Field fields = query.fields();
			fields.include("taskDetail." + SYS_OWNER.getName());
			List<Criteria> multiOr = new ArrayList<>();
			
			for (ColumnFormat columnFormat : columnFormatsPayment) {
				fields.include(columnFormat.getColumnName());
				
				if(!StringUtils.isBlank(req.getKeyword())) {
					if(columnFormat.getDataType() != null) {
						if(columnFormat.getDataType().equals("str")) {
							multiOr.add(Criteria.where(columnFormat.getColumnName()).regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));							
						} else if(columnFormat.getDataType().equals("num")) {
							//--: Ignore right now.
						}
					} else {
						LOG.debug(columnFormat.getColumnName() + "' dataType is null");
					}
				}
			}
			
			for (ColumnFormat columnFormat : taskDetailHeaders) {
				if(columnFormat.getColumnName().equals(SysFieldConstant.SYS_OWNER.getName())) continue;
				
				fields.include("taskDetail." + columnFormat.getColumnName());
				
				if(!StringUtils.isBlank(req.getKeyword())) {
					if(columnFormat.getDataType() != null) {
						if(columnFormat.getDataType().equals("str")) {
							multiOr.add(Criteria.where("taskDetail." + columnFormat.getColumnName()).regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));							
						} else if(columnFormat.getDataType().equals("num")) {
							//--: Ignore right now.
						}
					} else {
						LOG.debug(columnFormat.getColumnName() + "' dataType is null");
					}
				}
			}
			
			if(req.getDateFrom() != null) {
				if(req.getDateTo() != null) {
					criteria.and(paydateColName).gte(req.getDateFrom()).lte(req.getDateTo());			
				} else {
					criteria.and(paydateColName).gte(req.getDateFrom());
				}
			} else if(req.getDateTo() != null) {				
				criteria.and(paydateColName).lte(req.getDateTo());
			}
			
			Criteria[] multiOrArr = multiOr.toArray(new Criteria[multiOr.size()]);
			if(multiOrArr.length > 0) {
				criteria.orOperator(multiOrArr);				
			}
			
			//-------------------------------------------------------------------------------------
			LOG.debug("Start Count paymentDetail record");
			long totalItems = template.count(query, NEW_PAYMENT_DETAIL.getName());
			LOG.debug("End Count paymentDetail record");
			
			//-------------------------------------------------------------------------------------
			query = query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			
			/*if(StringUtils.isBlank(req.getColumnName())) {
				query.with(new Sort(Direction.DESC, StringUtils.isBlank(sortingColPayment) ? SYS_CREATED_DATE_TIME.getName() : sortingColPayment));
			} else {				
				query.with(new Sort(Direction.fromString(req.getOrder()), req.getColumnName()));
			}*/
			
			query.with(new Sort(Direction.DESC, StringUtils.isBlank(sortingColPayment) ? SYS_CREATED_DATE_TIME.getName() : sortingColPayment));
			
			LOG.debug("Start find paymentDetail");
			List<Map> paymentDetails = template.find(query, Map.class, NEW_PAYMENT_DETAIL.getName());			
			LOG.debug("End find paymentDetail");
			
			resp.setHeaders(columnFormatsPayment);
			resp.setTaskDetailHeaders(taskDetailHeaders);
			resp.setTotalItems(totalItems);
			resp.setPaymentDetails(paymentDetails);
			
			LOG.debug("End find");
			return resp;
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
	
}
