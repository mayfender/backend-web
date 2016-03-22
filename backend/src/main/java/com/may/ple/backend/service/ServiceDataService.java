package com.may.ple.backend.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.ServiceDataFindCriteriaReq;
import com.may.ple.backend.criteria.ServiceDataFindCriteriaResp;
import com.may.ple.backend.criteria.ServiceDataSaveCriteriaReq;
import com.may.ple.backend.entity.ServiceData;
import com.may.ple.backend.repository.ServiceDataRepository;

@Service
public class ServiceDataService {
	private static final Logger LOG = Logger.getLogger(ServiceDataService.class.getName());
	private ServiceDataRepository serviceDataRepository;
	private DataSource dataSource;
	private PrintManageService prinService;
	@PersistenceContext 
	private EntityManager em;
	
	@Autowired	
	public ServiceDataService(ServiceDataRepository serviceDataRepository, DataSource dataSource, PrintManageService prinService) {
		this.serviceDataRepository = serviceDataRepository;
		this.dataSource = dataSource;
		this.prinService = prinService;
	}
	
	public ServiceDataFindCriteriaResp findServiceData(ServiceDataFindCriteriaReq req) {
		String jpql = "select xxx from ServiceData s where serviceTypeId = " + req.getServiceTypeId() + " xxxx order by s.createdDateTime desc "; 
		String where = "";
		
		if(req.getDateTimeStart() != null) where += " and s.createdDateTime >= :startDate " ;
		if(req.getDateTimeEnd() != null) where += " and s.createdDateTime <= :endDate " ;
		if(!StringUtils.isBlank(req.getDocNo())) where += " and s.docNo like :docNo " ;
		if(req.getStatus() != null) where += " and s.status = :status " ;
		
		jpql = jpql.replace("xxxx", where);
		
		Query queryTotal = em.createQuery(jpql.replace("xxx", "count(s.id)"));
		
		if(req.getDateTimeStart() != null) queryTotal.setParameter("startDate", req.getDateTimeStart());
		if(req.getDateTimeEnd() != null) queryTotal.setParameter("endDate", req.getDateTimeEnd());
		if(!StringUtils.isBlank(req.getDocNo())) queryTotal.setParameter("docNo", "%" + req.getDocNo() + "%");
		if(req.getStatus() != null) queryTotal.setParameter("status", req.getStatus());
		
		long countResult = (long)queryTotal.getSingleResult();
		LOG.debug("Totol record: " + countResult);
		
		TypedQuery<ServiceData> query = em.createQuery(jpql.replace("xxx", "s"), ServiceData.class);
		
		if(req.getDateTimeStart() != null) query.setParameter("startDate", req.getDateTimeStart());
		if(req.getDateTimeEnd() != null) query.setParameter("endDate", req.getDateTimeEnd());
		if(!StringUtils.isBlank(req.getDocNo())) query.setParameter("docNo", "%" + req.getDocNo() + "%");
		if(req.getStatus() != null) query.setParameter("status", req.getStatus());
		
		int startRecord = (req.getCurrentPage() - 1) * req.getItemsPerPage();
		LOG.debug("Start get record: " + startRecord);
		
		query.setFirstResult(startRecord);
		query.setMaxResults(req.getItemsPerPage());
		List<ServiceData> resultList = query.getResultList();
		
		ServiceDataFindCriteriaResp resp = new ServiceDataFindCriteriaResp();
		resp.setTotalItems(countResult);
		resp.setServiceDatas(resultList);
		
		
		/*PageRequest page = new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage(), Sort.Direction.DESC, "createdDateTime");
		Page<ServiceData> pageData = null;
		
		if(req.getDateTimeStart() != null && req.getDateTimeEnd() != null && !StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndCreatedDateTimeBetweenAndDocNoContaining(page, req.getServiceTypeId(), req.getDateTimeStart(), req.getDateTimeEnd(), req.getDocNo());
			
		} else if(req.getDateTimeStart() != null && req.getDateTimeEnd() != null && StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndCreatedDateTimeBetween(page, req.getServiceTypeId(), req.getDateTimeStart(), req.getDateTimeEnd());
			
		} else if(req.getDateTimeStart() != null && req.getDateTimeEnd() == null && !StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndCreatedDateTimeGreaterThanEqualAndDocNoContaining(page, req.getServiceTypeId(), req.getDateTimeStart(), req.getDocNo());
			
		} else if(req.getDateTimeStart() != null && req.getDateTimeEnd() == null && StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndCreatedDateTimeGreaterThanEqual(page, req.getServiceTypeId(), req.getDateTimeStart());
			
		} else if(req.getDateTimeStart() == null && req.getDateTimeEnd() != null && !StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndCreatedDateTimeLessThanEqualAndDocNoContaining(page, req.getServiceTypeId(), req.getDateTimeEnd(), req.getDocNo());
			
		} else if(req.getDateTimeStart() == null && req.getDateTimeEnd() != null && StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndCreatedDateTimeLessThanEqual(page, req.getServiceTypeId(), req.getDateTimeEnd());
			
		} else if(req.getDateTimeStart() == null && req.getDateTimeEnd() == null && !StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndDocNoContaining(page, req.getServiceTypeId(), req.getDocNo());
			
		} else if(req.getDateTimeStart() == null && req.getDateTimeEnd() == null && StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeId(page, req.getServiceTypeId());		
			
		}*/
		
		return resp;
	}
	
	public void save(ServiceDataSaveCriteriaReq req) throws Exception {
		Date date = new Date();
		String docNoPrefix = null;
		
		if(req.getServiceTypeId() == 1) {
			docNoPrefix = "OE";
		} else if(req.getServiceTypeId() == 2) {
			docNoPrefix = "PS";
		} else if(req.getServiceTypeId() == 3) {
			docNoPrefix = "OO";
		} else if(req.getServiceTypeId() == 4) {
			docNoPrefix = "PV";
		} else if(req.getServiceTypeId() == 5) {
			docNoPrefix = "TB";
		} else {
			throw new Exception("Out of cases");
		}
		
		String docNo = String.format(docNoPrefix + "%1$tY%1$tm", date);
		List<ServiceData> serviceDatas = serviceDataRepository.findByDocNoContaining(docNo);
		docNo = String.format(docNo + "%03d", serviceDatas.size() + 1);
		LOG.debug("Last docNo: " + docNo);
		
		ServiceData serviceData = new ServiceData(docNo, req.getReceiver(), 
				req.getSender(), 
				req.getPostDest(), 
				req.getAmount(),
				req.getFee(), 
				req.getOtherServicePrice(), 
				req.getAccName(),  
				req.getBankName(), 
				req.getAccNo(), 
				req.getTel(), 
				req.getStatus(), 
				req.getServiceTypeId(),
				date, date);
		
		serviceDataRepository.save(serviceData);
	}
	
	public ServiceDataSaveCriteriaReq edit(Long id) throws Exception {
		ServiceData serviceData = serviceDataRepository.findOne(id);
		
		ServiceDataSaveCriteriaReq criteriaReq = new ServiceDataSaveCriteriaReq();
		criteriaReq.setSender(serviceData.getSender());
		criteriaReq.setReceiver(serviceData.getReceiver());
		criteriaReq.setPostDest(serviceData.getPostDest());
		criteriaReq.setAccName(serviceData.getAccName());
		criteriaReq.setAccNo(serviceData.getAccNo());
		criteriaReq.setBankName(serviceData.getBankName());
		criteriaReq.setTel(serviceData.getTel());
		criteriaReq.setAmount(serviceData.getAmount());
		criteriaReq.setFee(serviceData.getFee());
		criteriaReq.setOtherServicePrice(serviceData.getOtherServicePrice());
		criteriaReq.setStatus(serviceData.getStatus());
		criteriaReq.setId(serviceData.getId());
		
		return criteriaReq;
	}
	
	public void update(ServiceDataSaveCriteriaReq req) throws Exception {
		ServiceData serviceData = serviceDataRepository.findOne(req.getId());
		
		serviceData.setSender(req.getSender());
		serviceData.setReceiver(req.getReceiver());
		serviceData.setPostDest(req.getPostDest());
		serviceData.setAccName(req.getAccName());
		serviceData.setAccNo(req.getAccNo());
		serviceData.setBankName(req.getBankName());
		serviceData.setTel(req.getTel());
		serviceData.setAmount(req.getAmount());
		serviceData.setFee(req.getFee());
		serviceData.setOtherServicePrice(req.getOtherServicePrice());
		serviceData.setStatus(req.getStatus());
		serviceData.setUpdatedDateTime(new Date());
		
		serviceDataRepository.save(serviceData);
	}
	
	public void print(Long id) {
		ServiceData serviceData = serviceDataRepository.findOne(id);
		
		/*if(req.getServiceTypeId() == 1) {
		prinService.tananatEms(req);
	} else if(req.getServiceTypeId() == 2) {
		prinService.payService(req);
	} else if(req.getServiceTypeId() == 3) {
		prinService.tananatOnline(req);
	} else if(req.getServiceTypeId() == 4) {
		prinService.payVehicle(req);
	} else if(req.getServiceTypeId() == 5) {
		prinService.transfer(req);
	}*/
		
	}
	
}
