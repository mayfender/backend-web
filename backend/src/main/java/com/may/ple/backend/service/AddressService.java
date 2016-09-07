package com.may.ple.backend.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.AddressFindCriteriaReq;
import com.may.ple.backend.criteria.AddressSaveCriteriaReq;
import com.may.ple.backend.entity.Address;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;

@Service
public class AddressService {
	private static final Logger LOG = Logger.getLogger(AddressService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	
	@Autowired	
	public AddressService(MongoTemplate template, DbFactory dbFactory) {
		this.templateCore = template;
		this.dbFactory = dbFactory;
	}
	
	public List<Address> find(AddressFindCriteriaReq req) throws Exception {
		try {			
			if(!StringUtils.isBlank(req.getIdCardNo()) || !StringUtils.isBlank(req.getContractNo())) {
				MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
				Criteria criteria;
				
				if(StringUtils.isBlank(req.getIdCardNo())) {
					LOG.debug("Find by idCardNo");
					criteria = Criteria.where("idCardNo").in(req.getIdCardNo());
				} else {
					LOG.debug("Find by contractNo");
					criteria = Criteria.where("contractNo").in(req.getContractNo());
				}
	
				Query query = Query.query(criteria);
				query.fields()
				.include("name")
				.include("addr1")
				.include("addr2")
				.include("addr3")
				.include("addr4")
				.include("tel")
				.include("mobile")
				.include("fax");
				
				List<Address> addresses = template.find(query, Address.class);			
				
				return addresses;
			} else {
				return null;
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String save(AddressSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			
			LOG.debug("Get user");
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			Address addr;			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			if(StringUtils.isBlank(req.getId())) {
				addr = new Address(req.getName(), req.getAddr1(), req.getAddr2(), req.getAddr3(), req.getAddr4(), req.getTel(), req.getMobile(), req.getFax());
				addr.setCreatedDateTime(date);
				addr.setUpdatedDateTime(date);
				addr.setCreatedBy(user.getId());	
				addr.setIdCardNo(req.getIdCardNo());
				addr.setContractNo(req.getContractNo());
			} else {
				addr = template.findOne(Query.query(Criteria.where("id").is(req.getId())), Address.class);
				addr.setName(req.getName());
				addr.setAddr1(req.getAddr1());
				addr.setAddr2(req.getAddr2());
				addr.setAddr3(req.getAddr3());
				addr.setAddr4(req.getAddr4());
				addr.setTel(req.getTel());
				addr.setMobile(req.getMobile());
				addr.setFax(req.getFax());
				addr.setUpdatedDateTime(date);
				addr.setUpdatedBy(user.getId());
			}
			
			LOG.debug("Save");
			template.save(addr);
			
			return addr.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void delete(String id, String productId) throws Exception {
		try {
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			template.remove(Query.query(Criteria.where("id").is(id)), Address.class);
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
		
}
