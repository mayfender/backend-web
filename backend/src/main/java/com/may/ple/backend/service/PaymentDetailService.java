package com.may.ple.backend.service;

import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OLD_ORDER;

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

import com.may.ple.backend.criteria.PaymentDetailCriteriaReq;
import com.may.ple.backend.criteria.PaymentDetailCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.model.DbFactory;

@Service
public class PaymentDetailService {
	private static final Logger LOG = Logger.getLogger(PaymentDetailService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	
	@Autowired
	public PaymentDetailService(DbFactory dbFactory, MongoTemplate templateCenter) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
	}
	
	public PaymentDetailCriteriaResp find(PaymentDetailCriteriaReq req) throws Exception {
		try {
			LOG.debug("Start find");
			PaymentDetailCriteriaResp resp = new PaymentDetailCriteriaResp();
			
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(req.getProductId())), Product.class);
			List<ColumnFormat> columnFormatsPayment = product.getColumnFormatsPayment();
			ProductSetting productSetting = product.getProductSetting();
			String contractNoColumn = productSetting.getContractNoColumnNamePayment();				
			
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
			
			//-------------------------------------------------------------------------------------
			Query query = Query.query(criteria);
			Field fields = query.fields();
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
			
			Criteria[] multiOrArr = multiOr.toArray(new Criteria[multiOr.size()]);
			if(multiOrArr.length > 0) {
				criteria.orOperator(multiOrArr);				
			}
			
			//-------------------------------------------------------------------------------------
			LOG.debug("Start Count paymentDetail record");
			long totalItems = template.count(query, "paymentDetail");
			LOG.debug("End Count paymentDetail record");
			
			//-------------------------------------------------------------------------------------
			query = query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			
			if(StringUtils.isBlank(req.getColumnName())) {
				query.with(new Sort(SYS_OLD_ORDER.getName()));
			} else {				
				query.with(new Sort(Direction.fromString(req.getOrder()), req.getColumnName()));
			}
			
			LOG.debug("Start find paymentDetail");
			List<Map> paymentDetails = template.find(query, Map.class, "paymentDetail");			
			LOG.debug("End find paymentDetail");
			
			resp.setHeaders(columnFormatsPayment);
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
