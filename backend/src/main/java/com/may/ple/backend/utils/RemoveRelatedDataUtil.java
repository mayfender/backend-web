package com.may.ple.backend.utils;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_PAYMENT_DETAIL;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.may.ple.backend.entity.Address;
import com.may.ple.backend.entity.ImportMenu;
import com.may.ple.backend.entity.ImportOthersSetting;
import com.may.ple.backend.entity.TraceWork;
import com.may.ple.backend.entity.TraceWorkComment;
import com.may.ple.backend.entity.TraceWorkUpdatedHistory;

public class RemoveRelatedDataUtil {
	private static final Logger LOG = Logger.getLogger(RemoveRelatedDataUtil.class.getName());
	
	public static void allRelated(MongoTemplate template, List<String> contractNoVals, List<String> idCardVals, String contractNoColumnPayment) throws Exception {
		try {
			List<ImportMenu> menus = template.find(new Query(), ImportMenu.class);
			ImportOthersSetting menuSetting;
			List<Criteria> multiOr;
			Criteria[] multiOrArr;
			Criteria criteria;
			
			//---: Remove others menu
			for (ImportMenu importMenu : menus) {
				menuSetting = importMenu.getSetting();
				multiOr = new ArrayList<>();
				
				if(!StringUtils.isBlank(menuSetting.getContractNoColumnName())) {			
					multiOr.add(Criteria.where(menuSetting.getContractNoColumnName()).in(contractNoVals));
				}
				if(!StringUtils.isBlank(menuSetting.getIdCardNoColumnName())) {
					multiOr.add(Criteria.where(menuSetting.getIdCardNoColumnName()).in(idCardVals));
				}
				
				multiOrArr = multiOr.toArray(new Criteria[multiOr.size()]);
				criteria = new Criteria();
				criteria.orOperator(multiOrArr);
				
				template.remove(Query.query(criteria), importMenu.getId());
			}
			
			Query query = Query.query(Criteria.where("contractNo").in(contractNoVals));
			
			//---: Remove traceWord
			template.remove(query, Address.class);
			
			//---: Remove traceWork
			template.remove(query, TraceWork.class);
			
			//---: Remove traceWorkUpdatedHistory
			template.remove(query, TraceWorkUpdatedHistory.class);
			
			//---: Remove address
			template.remove(query, TraceWorkComment.class);
			
			//---: Remove payment
			if(!StringUtils.isBlank(contractNoColumnPayment)) {
				template.remove(Query.query(Criteria.where(contractNoColumnPayment).in(contractNoVals)), NEW_PAYMENT_DETAIL.getName());				
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
